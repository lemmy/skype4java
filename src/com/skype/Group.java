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
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * Implementation of the SKYPE GROUP object.
 * The GROUP object enables users to group contacts. There are two types of GROUP ; custom groups and hardwired groups.
 * @see https://developer.skype.com/Docs/ApiDoc/GROUP_object
 * @author Koji Hisano
 */
public final class Group extends SkypeObject {

	/**
	 * Enumeration of the type attribute.
	 */
    public enum Type {
    	/**
    	 * ALL_USERS - This group contains all users I know about, including users in my contactlist, users I recently contacted and blocked users.
		 * ALL_UserS - This group contains all contacts in my contactlist (also known as Users).
		 * SKYPE_UserS - This group contains Skype contacts in my contactlist.
		 * SkypeOut_UserS - This group contains SkypeOut contacts in my contactlist.
		 * ONLINE_UserS - This group contains Skype contacts in my contactlist who are online.
		 * UNKNOWN_OR_PENDINGAUTH_UserS - This group contains contacts in my contactlist who have not yet authorized me.
		 * RECENTLY_CONTACTED_USERS - This group contains contacts I have conversed with recently, including non-Users.
		 * USERS_WAITING_MY_AUTHORIZATION - This group contains contacts who are awating my response to an authorisation request, including non-Users.
		 * USERS_AUTHORIZED_BY_ME - This group contains all contacts I have authorised, including non-Users.
		 * USERS_BLOCKED_BY_ME - This group contains all contacts I have blocked, including non-Users.
		 * UNGROUPED_UserS - This group contains all contacts in my contactlist that do not belong to any custom group.
		 * CUSTOM_GROUP - This group type is reserved for user-defined groups. 
		 * SHARED_GROUP - @TODO: check API docs 
		 * PROPOSED_SHARED_GROUP - @TODO: check API docs
    	 */
        ALL_USERS, ALL_UserS, SKYPE_UserS, SKYPEOUT_UserS, ONLINE_UserS, UNKNOWN_OR_PENDINGAUTH_UserS, RECENTLY_CONTACTED_USERS, USERS_WAITING_MY_AUTHORIZATION, USERS_AUTHORIZED_BY_ME, USERS_BLOCKED_BY_ME, UNGROUPED_UserS, CUSTOM_GROUP, SHARED_GROUP, PROPOSED_SHARED_GROUP;
    }

    /**
     * ID of this GROUP.
     */
    private String id;

    /**
     * Constructor.
     * @param newId ID of this GROUP.
     */
    Group(final Connector aConnector, String newId) {
    	super(aConnector);
        this.id = newId;
    }

    /**
     * Return the ID as an hashcode.
     * @return ID of this GROUP.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Compare an other GROUP with this one based in their ID's.
     * @param compared the object to compare to.
     * @return true if object ID's are the same.
     */
    public boolean equals(Object compared) {
        if (this == compared) {
            return true;
        }
        if (compared instanceof Group) {
            return getId().equals(((Group) compared).getId());
        }
        return false;
    }

    /**
     * Return the GROUP ID as a string.
     * @return ID as a string.
     */
    public String toString() {
        return getId();
    }

    /**
     * Return the value of ID of this GROUP.
     * @return ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Add User to this GROUP.
     * @param User to add.
     * @throws SkypeException when the connection has gone bad.
     */
    public void addUser(User User) throws SkypeException {
        Utils.executeWithErrorCheck(connector, "ALTER GROUP " + getId() + " ADDUSER " + User.getId());
    }

    /**
     * Add a regular phonenumber (PSTN) to this group.
     * @param pstn the regular phonenumber.
     * @throws SkypeException when connection has gone bad.
     */
    public void addPSTN(String pstn) throws SkypeException {
        Utils.executeWithErrorCheck(connector, "ALTER GROUP " + getId() + " ADDUSER " + pstn);
    }

    /**
     * Remove a User from this GROUP.
     * @param User The User to remove from this group.
     * @throws SkypeException when connection has gone bad.
     */
    public void removeUser(User User) throws SkypeException {
        Utils.executeWithErrorCheck(connector, "ALTER GROUP " + getId() + " REMOVEUSER " + User.getId());
    }

    /**
     * Remove a regular phonenumber (PSTN) from this group.
     * @param pstn The number to remove from this group.
     * @throws SkypeException when the connection has gone bad.
     */
    public void removePSTN(String pstn) throws SkypeException {
        Utils.executeWithErrorCheck(connector, "ALTER GROUP " + getId() + " REMOVEUSER " + pstn);
    }

    /**
     * changes the display name for a contact.
     * @TODO: move this command to ContactList.java
     * @param User The User to change this for.
     * @param displayName The new name.
     * @throws SkypeException when connection has gone bad.
     */
    public void changeUserDisplayName(User User, String displayName) throws SkypeException {
        User.setDisplayName(displayName);
    }

    /**
     * changes the display name for a contact.
     * @TODO: move this command to ContactList.java
     * @param pstn The pstn to change this for.
     * @param displayName The new name.
     * @throws SkypeException when connection has gone bad.
     */
    public void changePSTNDisplayName(String pstn, String displayName) throws SkypeException {
        Utils.executeWithErrorCheck(connector, "SET USER " + pstn + " DISPLAYNAME " + displayName);
    }

    /**
     * Return all authorized users.
     * @return Array of Users.
     * @throws SkypeException when the connection has gone bad.
     */
    public User[] getAllUsers() throws SkypeException {
        String[] ids = Utils.convertToArray(getProperty("USERS"));
        User[] Users = new User[ids.length];
        for (int i = 0; i < ids.length; i++) {
            Users[i] = connector.getSkype().getContactList().getUser(ids[i]);
        }
        return Users;
    }

    /**
     * Check for any Users.
     * @param checked the User to check against.
     * @return True if User is authorized.
     * @throws SkypeException when connection has gone bad.
     */
    public boolean hasUser(User checked) throws SkypeException {
        for (User User : getAllUsers()) {
            if (checked.equals(User)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if GROUP is visible.
     * @return true if group is visible.
     * @throws SkypeException when connection has gone bad.
     */
    public boolean isVisible() throws SkypeException {
        return Boolean.parseBoolean(getProperty("VISIBLE"));
    }

    /**
     * Check if GROUP is expanded.
     * @return true if group is expanded.
     * @throws SkypeException when the connection has gone bad.
     */
    public boolean isExpanded() throws SkypeException {
        return Boolean.parseBoolean(getProperty("EXPANDED"));
    }

    /**
     * Return the displayname of this GROUP.
     * @return the displayname of this group.
     * @throws SkypeException when the connection has gone bad.
     */
    public String getDisplayName() throws SkypeException {
        return getProperty("DISPLAYNAME");
    }

    /**
     * Set the displayname of this GROUP.
     * @param newValue the new name.
     * @throws SkypeException when the connection has gone bad.
     */
    public void setDisplayName(String newValue) throws SkypeException {
        setProperty("DISPLAYNAME", newValue);
    }

    /**
     * Get the type of this GROUP.
     * @return the group type.
     * @throws SkypeException when the connection has gone bad.
     */
    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    /**
     * Retrieve a property of this GROUP.
     * @param name name of the property.
     * @return the value of this property.
     * @throws SkypeException when the connection has gone bad.
     */
    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty(connector, "GROUP", getId(), name);
    }

    /** 
     * Set a property value for this GROUP.
     * @param name name of the property.
     * @param newValue value of the property.
     * @throws SkypeException when the connection has gone bad.
     */
    private void setProperty(String name, String newValue) throws SkypeException {
        Utils.setProperty(connector, "GROUP", getId(), name, newValue);
    }

    /**
     * Remove this GROUP.
     * @throws SkypeException when the connection has gone bad.
     */
    public void dispose() throws SkypeException {
        try {
            String response = connector.execute("DELETE GROUP " + getId(), "DELETED GROUP ");
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
