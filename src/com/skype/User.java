/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * The <code>User</code> class contains the skype user's information.
 * <p>
 * For example, you can show the full name of the 'echo123' user by this code:
 * <pre>System.out.println(new User("echo123").getFullName());</pre>
 * </p>
 */
public class User {
    /**
     * The <code>Status</code> enum contains the online status constants of the skype user.
     * @see User#getOnlineStatus()
     */
    public enum Status {
        /**
         * The <code>UNKNOWN</code> constant indicates the skype user status is unknown.
         */
        UNKNOWN,
        /**
         * The <code>OFFLINE</code> constant indicates the skype user is offline.
         */
        OFFLINE,
        /**
         * The <code>ONLINE</code> constant indicates the skype user is online.
         */
        ONLINE,
        /**
         * The <code>AWAY</code> constant indicates the skype user is away.
         */
        AWAY,
        /**
         * The <code>NA</code> constant indicates the skype user is not available.
         */
        NA,
        /**
         * The <code>DND</code> constant indicates the skype user is in do not disturb mode.
         */
        DND,
        /**
         * The <code>SKYPEOUT</code> constant indicates the skype user is in SkypeOut mode.
         */
        SKYPEOUT,
        /**
         * The <code>SKYPEME</code> constant indicates the skype user is in SkypeMe mode.
         */
        SKYPEME,
    }

    /**
     * The <code>Sex</code> enum contains the sex constants of the skype user.
     * @see User#getSex()
     */
    public enum Sex {
        /**
         * The <code>UNKNOWN</code> constant indicates the sex of the skype user is unknown.
         */
        UNKNOWN,
        /**
         * The <code>MALE</code> constant indicates the skype user is male.
         */
        MALE,
        /**
         * The <code>FEMALE</code> constant indicates the skype user is female.
         */
        FEMALE;
    }

    private String id;

    /*
     * HANDLE HASCALLEQUIPMENT BUDDYSTATUS, ISAUTHORIZED ISBLOCKED
     * LASTONLINETIMESTAMP
     */
    public User(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (compared instanceof User) {
            User comparedUser = (User)compared;
            return getId().equals(comparedUser.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return getId();
    }

    public String getId() {
        return id;
    }

    public String getFullName() throws SkypeException {
        return getProperty("FULLNAME");
    }

    public Date getBirthDay() throws SkypeException {
        String value = getProperty("BIRTHDAY");
        if ("0".equals(value)) {
            return null;
        } else {
            try {
                return new SimpleDateFormat("yyyyMMdd").parse(value);
            } catch (ParseException e) {
                throw new IllegalStateException("library developer should check Skype specification.");
            }
        }
    }

    public Sex getSex() throws SkypeException {
        return Sex.valueOf((getProperty("SEX")));
    }

    public Status getOnlineStatus() throws SkypeException {
        return Status.valueOf((getProperty("ONLINESTATUS")));
    }

    public String getLauguage() throws SkypeException {
        return getProperty("LANGUAGE");
    }

    public String getCountry() throws SkypeException {
        return getProperty("COUNTRY");
    }

    public String getProvince() throws SkypeException {
        return getProperty("PROVINCE");
    }

    public String getCity() throws SkypeException {
        return getProperty("CITY");
    }

    public String getHomePhone() throws SkypeException {
        return getProperty("PHONE_HOME");
    }

    public String getOfficePhone() throws SkypeException {
        return getProperty("PHONE_OFFICE");
    }

    public String getMobilePhone() throws SkypeException {
        return getProperty("PHONE_MOBILE");
    }

    public String getHomePageAddress() throws SkypeException {
        return getProperty("HOMEPAGE");
    }

    public String getAbout() throws SkypeException {
        return getProperty("ABOUT");
    }

    public String getDisplayName() throws SkypeException {
        return getProperty("DISPLAYNAME");
    }

    public boolean isVideoCapable() {
        return Boolean.parseBoolean("IS_VIDEO_CAPABLE");
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("USER", getId(), name);
    }

    public Call call() throws SkypeException {
        return Skype.call(getId());
    }

    public Chat chat() throws SkypeException {
        return Skype.chat(getId());
    }

    public ChatMessage send(String message) throws SkypeException {
        return Skype.chat(getId()).send(message);
    }

    public VoiceMail leaveVoiceMail() throws SkypeException {
        return Skype.leaveVoiceMail(getId());
    }

    public void setDisplayName(String displayName) throws SkypeException {
        Utils.setProperty("USER", getId(), "DISPLAYNAME", displayName);
    }

    public ChatMessage[] getAllChatMessages() throws SkypeException {
        String[] ids = getHistory("CHATMESSAGES");
        ChatMessage[] messages = new ChatMessage[ids.length];
        for (int i = 0; i < ids.length; i++) {
            messages[i] = new ChatMessage(ids[i]);
        }
        List<ChatMessage> messageList = Arrays.asList(messages);
        Collections.reverse(messageList);
        return messageList.toArray(new ChatMessage[0]);
    }

    public Call[] getAllCalls() throws SkypeException {
        String[] ids = getHistory("CALLS");
        Call[] calls = new Call[ids.length];
        for (int i = 0; i < ids.length; i++) {
            calls[i] = Call.getCall(ids[i]);
        }
        return calls;
    }

    private String[] getHistory(String type) throws SkypeException {
        try {
            String responseHeader = type + " ";
            String response = Connector.getInstance().execute("SEARCH " + type + " " + getId(), responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            return Utils.convertToArray(data);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }
}
