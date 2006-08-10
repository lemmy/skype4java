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

import java.util.EventListener;

/**
 * Connector event listener interface.
 * Implement this interface when writing an event listener for connector classes.
 */
public interface ConnectorListener extends EventListener {
	/**
	 * This will be triggered when a connector has received a message.
	 * @param event the event source.
	 */
    void messageReceived(ConnectorMessageEvent event);
    
    /**
     * This will be triggered when a connector has send a message to a Skype Client.
     * @param event the event source.
     */
    void messageSent(ConnectorMessageEvent event);
    
    /**
     * This method will be triggered when the status of a connector changes.
     * @param event the event source.
     */
    void statusChanged(ConnectorStatusEvent event);
}
