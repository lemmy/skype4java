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

/**
 * Implementation of the ChatMessageListener.
 * Overide the methods to use for event trigger.
 * @see ChatMessageListener
 * @author Koji Hisano
 *
 */
public class ChatMessageAdapter implements ChatMessageListener {
    
	/**
	 * This method is called when a chatmessage is received.
	 * @param receivedChatMessage the actual message.
	 * @throws SkypeException when the connection has gone bad.
	 */
	public void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException {
    }

	/**
	 * This method is called when a chatmessage us sent.
	 * @param sentChatMessage the chatmessage that has been sent.
	 * @throws SkypeException when the connection has gone bad.
	 */
    public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
    }
}
