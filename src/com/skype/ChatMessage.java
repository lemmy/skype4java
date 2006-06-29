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

public final class ChatMessage {
    public enum Type {
        SETTOPIC, SAID, ADDEDMEMBERS, SAWMEMBERS, CREATEDCHATWITH, LEFT, UNKNOWN;
    }

    public enum Status {
        SENDING, SENT, RECEIVED, READ;
    }

    public enum LeaveReason {
        USER_NOT_FOUND, USER_INCAPABLE, ADDER_MUST_BE_FRIEND, ADDED_MUST_BE_AUTHORIZED, UNSUBSCRIBE;
    }

    private final String id;

    ChatMessage(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (compared instanceof ChatMessage) {
            ChatMessage comparedChatMessage = (ChatMessage)compared;
            return getId().equals(comparedChatMessage.getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    public User getSender() throws SkypeException {
        return new User(getSenderId());
    }

    public String getSenderId() throws SkypeException {
        return getProperty("FROM_HANDLE");
    }

    public String getSenderDisplayName() throws SkypeException {
        return getProperty("FROM_DISPNAME");
    }

    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getPropertyWithCommandId("CHATMESSAGE", getId(), "STATUS"));
    }

    public LeaveReason getLeaveReason() throws SkypeException {
        return LeaveReason.valueOf(getProperty("LEAVEREASON"));
    }

    public String getContent() throws SkypeException {
        return getProperty("BODY");
    }

    public Chat getChat() throws SkypeException {
        return new Chat(getProperty("CHATNAME"));
    }

    public User[] getAllUsers() throws SkypeException {
        String value = getProperty("USERS");
        if ("".equals(value)) {
            return new User[0];
        }
        String[] ids = value.split(" ");
        User[] users = new User[ids.length];
        for (int i = 0; i < ids.length; i++) {
            users[i] = new User(ids[i]);
        }
        return users;
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("CHATMESSAGE", getId(), name);
    }

    // TODO void setSeen()
    // TODO boolean isSeen()
}
