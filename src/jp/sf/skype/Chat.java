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

import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;

public final class Chat {
    public enum Status {
        DIALOG, MULTI_SUBSCRIBED;
    }

    private final String id;

    Chat(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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
