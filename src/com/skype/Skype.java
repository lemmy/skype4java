/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - good ideas for API and initial javadoc
 ******************************************************************************/
package com.skype;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.Connector.Status;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

/**
 * Skype information model (not view) class of Skype4Java.
 * Use this class staticly to do model actions (send messages, SMS messages or calls, etc).
 * @see SkypeClient
 * @author Koji Hisano
 */
public class Skype {
    /** contactList instance. */
    private ContactList contactList;
    
    /** Profile instance for this Skype session. */
    private Profile profile;

    /** chatMessageListener lock. */
    private Object chatMessageListenerMutex = new Object();
    /** CHATMESSAGE listener. */
    private ConnectorListener chatMessageListener;
    /** Collection of listeners. */
    private List<ChatMessageListener> chatMessageListeners = new CopyOnWriteArrayList<ChatMessageListener>();

	private Connector connector;

    public Skype(String user, String pass) {
    	connector = Connector.getInstance(this, user, pass);
	}
    /**
     * Check if Skype client is installed on this computer.
     * WARNING, does not work for all platforms yet.
     * @return true if Skype client is installed.
     */
    public boolean isInstalled() {
        String path = getInstalledPath();
        if(path == null) {
            return false;
        }
        return new File(path).exists();
    }

    /**
     * Find the install path of the Skype client.
     * WARNING, does not work for all platforms yet.
     * @return String with the full path to Skype client.
     */
    public String getInstalledPath() {
        return connector.getInstalledPath();
    }

    /**
     * Check if Skype client is running.
     * WARNING, does not work for all platforms.
     * @return true if Skype client is running.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public boolean isRunning() throws SkypeException {
        try {
            return connector.isRunning();
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    /**
     * Search users by a part of id or e-mail.
     * @param keword a part of id or e-mail
     * @return users
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public User[] searchUsers(String keyword) throws SkypeException {
        String command = "SEARCH USERS " + keyword;
        String responseHeader = "USERS ";
        String response;
        try {
            response = connector.executeWithId(command, responseHeader);
        } catch(ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
        Utils.checkError(response);
        String data = response.substring(responseHeader.length());
        String[] ids = Utils.convertToArray(data);
        User[] users = new User[ids.length];
        for(int i = 0; i < ids.length; ++i) {
            users[i] = getUser(ids[i]);
        }
        return users;
    }

    /**
     * Get the contactlist instance of this Skype session.
     * @return contactlist singleton.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public ContactList getContactList() throws SkypeException {
        if (contactList == null) {
            contactList = new ContactList(connector);
        }
        return contactList;
    }

    /**
     * Start a chat with multiple Skype users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to start a chat with.
     * @return The new chat object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public Chat chat(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return chat(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Start a chat with a single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to start the with.
     * @return The new chat.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public Chat chat(String skypeId) throws SkypeException {
        try {
            String responseHeader = "CHAT ";
            String response = connector.executeWithId("CHAT CREATE " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return getChat(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Get the singleton instance of the users profile.
     * @return Profile.
     */
    public synchronized Profile getProfile() {
        if (profile == null) {
            profile = new Profile(connector);
        }
        return profile;
    }


    /**
     * Gets the all chats.
     *
     * @return The all chats
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public Chat[] getAllChats() throws SkypeException {
        return getAllChats("CHATS");
    }

    /**
     * Gets the all chats which are open in the windows.
     *
     * @return The all chats which are open in the windows
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public Chat[] getAllActiveChats() throws SkypeException {
        return getAllChats("ACTIVECHATS");
    }

    /**
     * Gets the all chats which include unread messages
     *
     * @return The all chats which include unread messages
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public Chat[] getAllMissedChats() throws SkypeException {
        return getAllChats("MISSEDCHATS");
    }

    /**
     * Gets the all recent chats in the locally-cached history.
     *
     * @return The all recent chats in the locally-cached history
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public Chat[] getAllRecentChats() throws SkypeException {
        return getAllChats("RECENTCHATS");
    }

    /**
     * Gets the all chats by the type.
     *
     * @return The all chats by the type
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    private Chat[] getAllChats(String type) throws SkypeException {
        try {
            String command = "SEARCH " + type;
            String responseHeader = "CHATS ";
            String response = connector.execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Chat[] chats = new Chat[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chats[i] = getChat(ids[i]);
            }
            return chats;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Clears all chat history.
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public void clearChatHistory() throws SkypeException {
        Utils.executeWithErrorCheck(getConnector(),"CLEAR CHATHISTORY");
    }    

    /**
     * Add a listener for CHATMESSAGE events received from the Skype API.
     * @param listener the Listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #removeChatMessageListener(ChatMessageListener)
     */
    public void addChatMessageListener(ChatMessageListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.add(listener);
            if (chatMessageListener == null) {
                chatMessageListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("CHATMESSAGE ")) {
                            String data = message.substring("CHATMESSAGE ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("STATUS".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                ChatMessageListener[] listeners = chatMessageListeners.toArray(new ChatMessageListener[0]);
                                ChatMessage chatMessage = getChatMessage(id);
                                if ("SENT".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                            listener.chatMessageSent(chatMessage);
                                    }
                                } else if ("RECEIVED".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                            listener.chatMessageReceived(chatMessage);
                                    }
                                }
                            }
                        }
                    }
                };
                try {
                    connector.addConnectorListener(chatMessageListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    /**
     * Remove a listener for CHATMESSAGE events.
     * If the listener is already removed nothing happens.
     * @param listener The listener to remove.
     * @see #addChatMessageListener(ChatMessageListener)
     */
    public void removeChatMessageListener(ChatMessageListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.remove(listener);
            if (chatMessageListeners.isEmpty()) {
                connector.removeConnectorListener(chatMessageListener);
                chatMessageListener = null;
            }
        }
    }

    public void removeAllListeners() {
        synchronized(chatMessageListenerMutex) {
            chatMessageListeners.clear();
        }
    }

	public void dispose() {
		connector.dispose();
		connector = null;
		removeAllListeners();
	}

	public Connector getConnector() {
		return connector;
	}

	public Status getStatus() {
		return connector.getStatus();
	}

	public void addConnectorListener(
			ConnectorListener skypeConnectorListener) {
		connector.addConnectorListener(skypeConnectorListener, false);
	}

	public Status connect() {
		return connector.connect();
	}
	
	private final Map<String, Friend> idToFriends = new HashMap<String, Friend>();
    /**
     * @param string
     * @return
     */
    public Friend getFriend(final String anId) {
    	synchronized (idToFriends) {
    		Friend friend = idToFriends.get(anId);
    		if(friend == null) {
    			friend = new Friend(connector, anId);
    			idToFriends.put(anId, friend);
    		}
    		return friend;
		}
    }

	private final Map<String, User> idToUsers = new HashMap<String, User>();
    /**
     * Return User based on ID.
     * @param id ID of the User.
     * @return The user found.
     */
    public User getUser(final String anId) {
    	synchronized (idToUsers) {
    		User user = idToUsers.get(anId);
    		if(user == null) {
    			user = new User(connector, anId);
    			idToUsers.put(anId, user);
    		}
    		return user;
		}
    }

	private final Map<String, ChatMessage> idToChatMessages = new HashMap<String, ChatMessage>();
	/**
	 * @param string
	 * @return
	 */
	public ChatMessage getChatMessage(final String anId) {
		synchronized (idToChatMessages) {
			ChatMessage chatMessage = idToChatMessages.get(anId);
			if(chatMessage == null) {
				chatMessage = new ChatMessage(connector, anId);
				idToChatMessages.put(anId, chatMessage);
			}
			return chatMessage;
		}
	}

	private final Map<String, Chat> idToChats = new HashMap<String, Chat>();
    /**
     * @param id
     * @return
     */
    public Chat getChat(final String anId) {
    	synchronized (idToChats) {
    		Chat chat = idToChats.get(anId);
    		if(chat == null) {
    			chat = new Chat(connector, anId);
    			idToChats.put(anId, chat);
    		}
    		return chat;
		}
	}
    
	private final Map<String, Group> idToGroups = new HashMap<String, Group>();
	/**
	 * @param id
	 * @return
	 */
	public Group getGroup(final String anId) {
    	synchronized (idToGroups) {
    		Group group = idToGroups.get(anId);
    		if(group == null) {
    			group = new Group(connector, anId);
    			idToGroups.put(anId, group);
    		}
    		return group;
		}
	}
}
