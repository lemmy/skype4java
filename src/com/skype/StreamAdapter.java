/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the "Modified BSD license". which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

/**
 * Listener adapter for Stream objects. 
 */
public class StreamAdapter implements StreamListener {
	/**
	 * Overwrite this method to get triggered for each test message.
	 * @param receivedText the message received.
	 * @throws SkypeException when connection to Skype client has gone bad.
	 */
    public void textReceived(String receivedText) throws SkypeException {
    }

    /**
	 * Overwrite this method to get triggered for each datagram message.
	 * @param receivedDatagram the message received.
	 * @throws SkypeException when connection to Skype client has gone bad.
	 */
    public void datagramReceived(String receivedDatagram) throws SkypeException {
    }
}
