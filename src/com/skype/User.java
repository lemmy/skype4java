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
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * The <code>User</code> class contains the skype user's information.
 * <p>
 * For example, you can show the full name of the 'echo123' user by this code:
 * <pre>System.out.println(new User("echo123").getFullName());</pre>
 * </p>
 */
public class User extends SkypeObject {
    /**
     * Collection of User objects.
     */
    private static final Map<String, User> users = new HashMap<String, User>();
    
    /**
     * Returns the User object by the specified id.
     * @param id whose associated User object is to be returned.
     * @return User object with ID == id.
     */
    static User getInstance(final String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                users.put(id, new User(id));
            }
            return users.get(id);
        }
    }
    
    /**
     * Returns the Friend object by the specified id.
     * @param id whose associated Friend object is to be returned.
     * @return Friend object with ID == id.
     */
    static Friend getFriendInstance(String id) {
        synchronized(users) {
            if (!users.containsKey(id)) {
                Friend friend = new Friend(id);
                users.put(id, friend);
                return friend;
            } else {
                User user = users.get(id);
                if (user instanceof Friend) {
                    return (Friend)user;
                } else {
                    Friend friend = new Friend(id);
                    friend.copyFrom(user);
                    users.put(id, friend);
                    return friend;
                }
            }
        }
    }

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

    /** ID of this User. */
    private String id;

    /**
     * Constructor.
     * @param newId The USER ID.
     */
    User(String newId) {
        this.id = newId;
    }

    /**
     * Overridden to provide ID as hashcode.
     * @return ID.
     */
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * Overridden to compare User obejct based on ID.
     * @param compared the User to compare to.
     * @return true if ID's are equal.
     */
    public final boolean equals(Object compared) {
        if (compared instanceof User) {
            User comparedUser = (User)compared;
            return getId().equals(comparedUser.getId());
        }
        return false;
    }

    /**
     * Provide ID as string representation.
     * @return ID.
     */
    public final String toString() {
        return getId();
    }

    /**
     * Return ID of this User.
     * @return ID.
     */
    public final String getId() {
        return id;
    }

    /**
     * Return full name of this User.
     * @return String with fullname.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getFullName() throws SkypeException {
        return getProperty("FULLNAME");
    }

    /**
     * Return the birthdate of this User.
     * @return Date of birthday.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Date getBirthDay() throws SkypeException {
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

    /**
     * Return the sex of this User.
     * @return Sex of this User.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Sex getSex() throws SkypeException {
        return Sex.valueOf((getProperty("SEX")));
    }

    /**
     * Return the online status of this User.
     * @return Status of this User.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Status getOnlineStatus() throws SkypeException {
        return Status.valueOf((getProperty("ONLINESTATUS")));
    }

    /**
     * Return the native language of this User.
     * @return String with native language.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getLauguage() throws SkypeException {
        return getProperty("LANGUAGE");
    }

    /**
     * Return the country the User is based.
     * @return String with country.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getCountry() throws SkypeException {
        return getProperty("COUNTRY");
    }

    /**
     * Return the province the user is based.
     * @return String with the province the user is based.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getProvince() throws SkypeException {
        return getProperty("PROVINCE");
    }

    /**
     * Return the city this User is based in.
     * @return String with the city name the User is based in.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getCity() throws SkypeException {
        return getProperty("CITY");
    }

    /**
     * Return the home phone number that is in the User profile.
     * @return String with Home phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getHomePhone() throws SkypeException {
        return getProperty("PHONE_HOME");
    }

    /**
     * Return the office phone number that is in the User profile.
     * @return String with office phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getOfficePhone() throws SkypeException {
        return getProperty("PHONE_OFFICE");
    }

    /**
     * Return the mobile phone number of this User.
     * @return String with mobile phone number.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getMobilePhone() throws SkypeException {
        return getProperty("PHONE_MOBILE");
    }

    /**
     * Return the homepage URL of this User.
     * @return String with URL of homepage.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getHomePageAddress() throws SkypeException {
        return getProperty("HOMEPAGE");
    }

    /**
     * Return extra information User has provided in his/her profile.
     * @return STring with extra info.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getAbout() throws SkypeException {
        return getProperty("ABOUT");
    }

    /**
     * Return the displayname of this User.
     * @return String with displayname.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final String getDisplayName() throws SkypeException {
        return getProperty("DISPLAYNAME");
    }

    /**
     * Check if this User has a Skype client that can do video chats.
     * @return true if User can do videochats.
     */
    public final boolean isVideoCapable() {
        return Boolean.parseBoolean("IS_VIDEO_CAPABLE");
    }

    /**
     * Method used by other methods to retrieve a property value from Skype client.
     * @param name name of the property.
     * @return value of the property.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("USER", getId(), name);
    }

    /**
     * Start a call to this User.
     * @return new Call object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Call call() throws SkypeException {
        return Skype.call(getId());
    }

    /**
     * Start a chat to this User.
     * @return new Chat object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Chat chat() throws SkypeException {
        return Skype.chat(getId());
    }

    /**
     * Send this User a chatMessage.
     * @param message The message to send.
     * @return the new chatMessage object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final ChatMessage send(String message) throws SkypeException {
        return Skype.chat(getId()).send(message);
    }

    /**
     * Leave a voicemail for this User.
     * @return new VoiceMail object.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final VoiceMail leaveVoiceMail() throws SkypeException {
        return Skype.leaveVoiceMail(getId());
    }

    /**
     * Set a displayname for this User.
     * @param displayName the new name to set.
     * @throws SkypeException  when connection to Skype client has gone bad.
     */
    public final void setDisplayName(String displayName) throws SkypeException {
        Utils.setProperty("USER", getId(), "DISPLAYNAME", displayName);
    }

    /**
     * Search for all chatMessages to and from this User.
     * @return array of Chatmessages found.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final ChatMessage[] getAllChatMessages() throws SkypeException {
        String[] ids = getHistory("CHATMESSAGES");
        ChatMessage[] messages = new ChatMessage[ids.length];
        for (int i = 0; i < ids.length; i++) {
            messages[i] = ChatMessage.getInstance(ids[i]);
        }
        List<ChatMessage> messageList = Arrays.asList(messages);
        Collections.reverse(messageList);
        return messageList.toArray(new ChatMessage[0]);
    }

    /**
     * Search all calls to and from this User.
     * @return an array of found calls.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
    public final Call[] getAllCalls() throws SkypeException {
        String[] ids = getHistory("CALLS");
        Call[] calls = new Call[ids.length];
        for (int i = 0; i < ids.length; i++) {
            calls[i] = Call.getInstance(ids[i]);
        }
        return calls;
    }

    /**
     * Search the history with this user.
     * @param type Specify which history to search for.
     * @return an String array with found events.
     * @throws SkypeException when connection to Skype client has gone bad.
     */
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

    /**
     * Remove this User from the list of watchable Users.
     */
    final void dispose() {
        users.remove(getId());
    }
}
