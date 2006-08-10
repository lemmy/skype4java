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
package com.skype;

/**
 * Listener interface for Stream object events.
 * @see Stream
 * @see StreamAdapter
 */
public interface StreamListener {
    /**
     * This method will be fired when a text message is received.
     * @param receivedText the received message.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
	void textReceived(String receivedText) throws SkypeException;
    
	/**
     * This method will be fired when a datagram message is received.
     * @param receivedDatagram the received message.
     * @throws SkypeException when the connection to the Skype client has gone bad.
     */
	void datagramReceived(String receivedDatagram) throws SkypeException;
}
