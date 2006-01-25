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

import java.util.Date;

public final class Message {
    public enum Type {
        AUTHREQUEST, TEXT, CONTACTS, UNKNOWN;
    }

    public enum Status {
        SENDING, SENT, FAILED, RECEIVED, READ, IGNORED, QUEUED;
    }
    private final String id;

    Message(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Date getStartTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    public User getParter() throws SkypeException {
        return new User(getPartnerId());
    }

    public String getPartnerId() throws SkypeException {
        return getProperty("PARTNER_HANDLE");
    }

    public String getPartnerDisplayName() throws SkypeException {
        return getProperty("PARTNER_DISPNAME");
    }

    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    public Type getStatus() throws SkypeException {
        return Type.valueOf(getProperty("STATUS"));
    }

    public int getFailureReasonCode() throws SkypeException {
        return Integer.parseInt(getProperty("FAILUREREASON"));
    }

    public String getMessage() throws SkypeException {
        return getProperty("BODY");
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("MESSAGE", getId(), name);
    }
}
