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

/**
 * Abstract basis for all platform dependant connectorLinstener implementations.
 */
public abstract class AbstractConnectorListener implements ConnectorListener {
	/**
	 * This method gets fired when a Message is received.
	 * @param event The event that triggered this.
	 */
    public void messageReceived(ConnectorMessageEvent event) {
    }

    /**
     * This method gets fired when a message is being send.
	 * @param event The event that triggered this.
     */
    public void messageSent(ConnectorMessageEvent event) {
    }
    
    /**
     * This method is called when a status of a connector changes.
	 * @param event The event that triggered this. 
     */
    public void statusChanged(ConnectorStatusEvent event) {
    }
}
