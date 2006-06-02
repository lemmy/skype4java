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

public final class Group {
    public enum Type {
        ALL_USERS, ALL_FRIENDS, SKYPE_FRIENDS, SKYPEOUT_FRIENDS, ONLINE_FRIENDS, UNKNOWN_OR_PENDINGAUTH_FRIENDS, RECENTLY_CONTACTED_USERS, USERS_WAITING_MY_AUTHORIZATION, USERS_AUTHORIZED_BY_ME, USERS_BLOCKED_BY_ME, UNGROUPED_FRIENDS, CUSTOM_GROUP;
    }

    private String id;

    Group(String id) {
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
        if (compared instanceof Group) {
            return getId().equals(((Group) compared).getId());
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

    public void addFriend(Friend friend) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " ADDUSER " + friend.getId());
    }

    public void addPSTN(String pstn) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " ADDUSER " + pstn);
    }

    public void removeFriend(Friend friend) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " REMOVEUSER " + friend.getId());
    }

    public void removePSTN(String pstn) throws SkypeException {
        Utils.executeWithErrorCheck("ALTER GROUP " + getId() + " REMOVEUSER " + pstn);
    }

    public void changeFriendDisplayName(Friend friend, String displayName) throws SkypeException {
        friend.setDisplayName(displayName);
    }

    public void changePSTNDisplayName(String pstn, String displayName) throws SkypeException {
        Utils.executeWithErrorCheck("SET USER " + pstn + " DISPLAYNAME " + displayName);
    }

    public Friend[] getAllFriends() throws SkypeException {
        String[] ids = Utils.convertToArray(getProperty("USERS"));
        Friend[] friends = new Friend[ids.length];
        for (int i = 0; i < ids.length; i++) {
            friends[i] = Skype.getContactList().getFriend(ids[i]);
        }
        return friends;
    }

    public boolean hasFriend(Friend checked) throws SkypeException {
        for (Friend friend : getAllFriends()) {
            if (checked.equals(friend)) {
                return true;
            }
        }
        return false;
    }

    public boolean isVisible() throws SkypeException {
        return Boolean.parseBoolean(getProperty("VISIBLE"));
    }

    public boolean isExpanded() throws SkypeException {
        return Boolean.parseBoolean(getProperty("EXPANDED"));
    }

    public String getDisplayName() throws SkypeException {
        return getProperty("DISPLAYNAME");
    }

    public void setDisplayName(String newValue) throws SkypeException {
        setProperty("DISPLAYNAME", newValue);
    }

    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("GROUP", getId(), name);
    }

    private void setProperty(String name, String newValue) throws SkypeException {
        Utils.setProperty("GROUP", getId(), name, newValue);
    }

    public void dispose() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("DELETE GROUP " + getId(), "DELETED GROUP ");
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
