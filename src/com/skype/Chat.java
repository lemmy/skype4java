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

    public ChatMessage send(String message) throws SkypeException {
        try {
            String responseHeader = "CHATMESSAGE ";
            String response = Connector.getInstance().execute("CHATMESSAGE " + getId() + " " + message, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return new ChatMessage(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }
}
