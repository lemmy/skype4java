/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.util.Date;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public final class Chat {
    public enum Status {
        // TODO examine when LEGACY_DIALOG is used
        DIALOG, LEGACY_DIALOG, MULTI_SUBSCRIBED, UNSUBSCRIBED;
    }

    private final String id;

    Chat(String id) {
        assert id != null;
        this.id = id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (compared instanceof Chat) {
            return getId().equals(((Chat) compared).getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setTopic(String newValue) throws SkypeException {
        try {
            String command = "ALTER CHAT " + getId() + " SETTOPIC " + newValue;
            String responseHeader = "ALTER CHAT SETTOPIC";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public void addUser(User addedUser) throws SkypeException {
        Utils.checkNotNull("addedUser", addedUser);
        addUsers(new User[] {addedUser});
    }

    public void addUsers(User[] addedUsers) throws SkypeException {
        Utils.checkNotNull("addedUsers", addedUsers);
        try {
            String command = "ALTER CHAT " + getId() + " ADDMEMBERS " + toCommaSeparatedString(addedUsers);
            String responseHeader = "ALTER CHAT ADDMEMBERS";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    private static String toCommaSeparatedString(User[] users) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < users.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(users[i].getId());
        }
        return builder.toString();
    }

    public void leave() throws SkypeException {
        try {
            String command = "ALTER CHAT " + getId() + " LEAVE";
            String responseHeader = "ALTER CHAT LEAVE";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public ChatMessage[] getAllChatMessages() throws SkypeException {
        try {
            String command = "GET CHAT " + getId() + " CHATMESSAGES";
            String responseHeader = "CHAT " + getId() + " CHATMESSAGES ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            ChatMessage[] chatMessages = new ChatMessage[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chatMessages[i] = new ChatMessage(ids[i]);
            }
            return chatMessages;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    public ChatMessage[] getRecentChatMessages() throws SkypeException {
        try {
            String command = "GET CHAT " + getId() + " RECENTCHATMESSAGES";
            String responseHeader = "CHAT " + getId() + " RECENTCHATMESSAGES ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            ChatMessage[] chatMessages = new ChatMessage[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chatMessages[i] = new ChatMessage(ids[i]);
            }
            return chatMessages;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    public ChatMessage send(String message) throws SkypeException {
        try {
            String responseHeader = "CHATMESSAGE ";
            String response = Connector.getInstance().executeWithId("CHATMESSAGE " + getId() + " " + message, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return new ChatMessage(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    public User getAdder() throws SkypeException {
        String adder = getProperty("ADDER");
        if ("".equals(adder)) {
            return null;
        } else {
            return new User(adder);
        }
    }

    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getPropertyWithCommandId("CHAT", getId(), "STATUS"));
    }

    public String getWindowTitle() throws SkypeException {
        return getProperty("FRIENDLYNAME");
    }

    public User[] getAllPosters() throws SkypeException {
        return getUsersProperty("POSTERS");
    }

    public User[] getAllMembers() throws SkypeException {
        return getUsersProperty("MEMBERS");
    }

    // TODO examine what are active members
    public User[] getAllActiveMembers() throws SkypeException {
        return getUsersProperty("ACTIVEMEMBERS");
    }

    private User[] getUsersProperty(String name) throws SkypeException {
        try {
            String command = "GET CHAT " + getId() + " " + name;
            String responseHeader = "CHAT " + id + " " + name + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            if ("".equals(data)) {
                return new User[0];
            }
            String[] ids = data.split(" ");
            User[] users = new User[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                users[i] = new User(ids[i]);
            }
            return users;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("CHAT", getId(), name);
    }
}
