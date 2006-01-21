/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;

public final class Friend {
    public enum Status {
        UNKNOWN, OFFLINE, ONLINE, AWAY, NA, DND, SKYPEOUT, SKYPEME;
    }

    public enum Sex {
        UNKNOWN, MALE, FEMALE;
    }

    private String id;

    /*
     * HANDLE HASCALLEQUIPMENT BUDDYSTATUS, ISAUTHORIZED ISBLOCKED
     * LASTONLINETIMESTAMP
     */
    Friend(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (this == compared) {
            return true;
        }
        if (compared instanceof Friend) {
            return getId().equals(((Friend) compared).getId());
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

    public Message send(String message) throws SkypeException {
        return Skype.chat(getId()).send(message);
    }

    public VoiceMail leaveVoiceMail() throws SkypeException {
        return Skype.leaveVoiceMail(getId());
    }

    public void setDisplayName(String displayName) throws SkypeException {
        Utils.setProperty("USER", getId(), "DISPLAYNAME", displayName);
    }
    
    public Message[] getAllMessages() throws SkypeException {
        String[] ids = getHistory("MESSAGES");
        Message[] messages = new Message[ids.length];
        for (int i = 0; i < ids.length; i++) {
            messages[i] = new Message(ids[i]);
        }
        List<Message> messageList = Arrays.asList(messages);
        Collections.reverse(messageList);
        return messageList.toArray(new Message[0]);
    }

    public Call[] getAllCalls() throws SkypeException {
        String[] ids = getHistory("CALLS");
        Call[] calls = new Call[ids.length];
        for (int i = 0; i < ids.length; i++) {
            calls[i] = new Call(ids[i]);
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
