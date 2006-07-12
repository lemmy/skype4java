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
package com.skype.connector;

public abstract class MessageProcessor {
    private Object lock;

    private ConnectorMessageReceivedListener parent;

    final void init(Object lock, ConnectorMessageReceivedListener parent) {
        this.lock = lock;
    }

    protected abstract void messageReceived(String message);

    protected final void releaseLock() {
        synchronized (lock) {
            lock.notify();
        }
    }

    protected final void processedAllMessages() {
        Connector.getInstance().removeConnectorMessageReceivedListener(parent);
    }
}
