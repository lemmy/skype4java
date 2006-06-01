/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;

public final class ContactList {
    ContactList() {
    }

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

    public Friend getFriend(String skypeId) throws SkypeException {
        Utils.checkNotNull(skypeId, "skypeId");
        for (Friend friend : getAllFriends()) {
            if (skypeId.equals(friend.getId())) {
                return friend;
            }
        }
        return null;
    }

    public Group[] getAllSystemGroups() throws SkypeException {
        return getAllGroups("HARDWIRED");
    }

    public Group getSysteGroup(Group.Type type) throws SkypeException {
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

    public Group[] getAllGroups() throws SkypeException {
        return getAllGroups("CUSTOM");
    }

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

    public Group getGroup(String displayName) throws SkypeException {
        Utils.checkNotNull(displayName, "displayName");
        for (Group group : getAllGroups()) {
            if (displayName.equals(group.getDisplayName())) {
                return group;
            }
        }
        return null;
    }

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

    public void removeGroup(Group group) throws SkypeException {
        group.dispose();
    }
}
