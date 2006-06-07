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

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public final class Chat {
    public enum Status {
        DIALOG, MULTI_SUBSCRIBED;
    }

    private final String id;

    Chat(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (compared instanceof Chat) {
            return id.equals(((Chat) compared).id);
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public void setTopic(String newValue) throws SkypeException {
        try {
            String command = "ALTER CHAT " + id + " SETTOPIC " + newValue;
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
            String command = "ALTER CHAT " + id + " ADDMEMBERS " + toCommaSeparatedString(addedUsers);
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
            String command = "ALTER CHAT " + id + " LEAVE";
            String responseHeader = "ALTER CHAT LEAVE";
            String response = Connector.getInstance().execute(command, responseHeader);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public ChatMessage[] getAllChatMessages() throws SkypeException {
        try {
            String command = "GET CHAT " + id + " CHATMESSAGES";
            String responseHeader = "CHAT " + id + " CHATMESSAGES ";
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
            String command = "GET CHAT " + id + " RECENTCHATMESSAGES";
            String responseHeader = "CHAT " + id + " RECENTCHATMESSAGES ";
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
}
