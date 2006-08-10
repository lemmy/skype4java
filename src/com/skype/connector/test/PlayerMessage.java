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
package com.skype.connector.test;

public final class PlayerMessage {
    public enum Type {
        RECEIVED, SENT;
    }

    private final Type type;
    private final long time;
    private final String message;

    public PlayerMessage(final Type type, final long time, final String message) {
        this.type = type;
        this.time = time;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }

    public Type getType() {
        return type;
    }
}
