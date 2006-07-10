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

/**
 * This object can be used for all actions normal to a contactlist, like searching users and friends.
 * @author Koji Hisano.
 *
 */
public final class ContactList {
	/**
	 * Constructor.
	 *
	 */
    ContactList() {
    }

    /**
     * Get all authorized users.
     * @return array of friends.
     * @throws SkypeException when the connection has gone bad.
     */
    public Friend[] getAllFriends() throws SkypeException {
        try {
            String responseHeader = "USERS ";
            String response = Connector.getInstance().execute("SEARCH FRIENDS", responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Friend[] friends = new Friend[ids.length];
            for (int i = 0; i < ids.length; i++) {
                friends[i] = new Friend(ids[i]);
            }
            return friends;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Get the Friend object for one of the authorized users.
     * @param skypeId Skype ID of the friend.
     * @return the friend or null if friend isn't found.
     * @throws SkypeException when a connection has gone bad.
     */
    public Friend getFriend(String skypeId) throws SkypeException {
        Utils.checkNotNull(skypeId, "skypeId");
        for (Friend friend : getAllFriends()) {
            if (skypeId.equals(friend.getId())) {
                return friend;
            }
        }
        return null;
    }


    /**
     * Search for HARDWIRED groups.
     * @return array of found groups or null if groups could not be found.
     * @throws SkypeException when connection has gone bad.
     */
    public Group[] getAllSystemGroups() throws SkypeException {
        return getAllGroups("HARDWIRED");
    }

    /**
     * Search for a group based on it's type.
     * @param type Group type to find.
     * @return group or null if group isn't found.
     * @throws SkypeException when connection has gone bad or type isn't correct, like CUSTOM_GROUP.
     */
    public Group getSystemGroup(Group.Type type) throws SkypeException {
        if (type == Group.Type.CUSTOM_GROUP) {
            throw new IllegalArgumentException("custom type is not supported (use getAllGroups method to resolve)");
        }
        for (Group group : getAllSystemGroups()) {
            if (group.getType() == type) {
                return group;
            }
        }
        return null;
    }

    /**
     * Search for CUSTOM groups.
     * @return Array of found groups.
     * @throws SkypeException when connection has gone bad.
     */
    public Group[] getAllGroups() throws SkypeException {
        return getAllGroups("CUSTOM");
    }

    /**
     * Search for groups based on type.
     * @param type the type to search for.
     * @return Array of groups with type.
     * @throws SkypeException when connection has gone bad.
     */
    private Group[] getAllGroups(String type) throws SkypeException {
        try {
            String responseHeader = "GROUPS ";
            String response = Connector.getInstance().execute("SEARCH GROUPS " + type, responseHeader);
            Utils.checkError(response);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Group[] groups = new Group[ids.length];
            for (int i = 0; i < ids.length; i++) {
                groups[i] = new Group(ids[i]);
            }
            return groups;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Search for group with displayname.
     * @param displayName name of the group.
     * @return found Group or null if none is found.
     * @throws SkypeException when connection has gone bad.
     */
    public Group getGroup(String displayName) throws SkypeException {
        Utils.checkNotNull(displayName, "displayName");
        for (Group group : getAllGroups()) {
            if (displayName.equals(group.getDisplayName())) {
                return group;
            }
        }
        return null;
    }

    /**
     * Add group to contactlist.
     * @param name Name of the group.
     * @return the Group object created.
     * @throws SkypeException when connection has gone bad.
     */
    public Group addGroup(String name) throws SkypeException {
        try {
            String responseHeader = "GROUP ";
            String response = Connector.getInstance().execute("CREATE GROUP " + name, responseHeader).substring(responseHeader.length());
            Utils.checkError(response);
            String id = response.substring(0, response.indexOf(' '));
            return new Group(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /** 
     * Remove group from contactlist.
     * @param group Group to remove.
     * @throws SkypeException when connection has gone bad.
     */
    public void removeGroup(Group group) throws SkypeException {
        group.dispose();
    }
}
