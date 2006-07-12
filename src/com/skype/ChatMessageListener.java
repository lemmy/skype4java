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
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

/**
 * This is the listener for the ChatMessage object.
 * @see ChatMessage
 * @author Koji Hisano
 */
public interface ChatMessageListener {
	/**
	 * This method is called when a ChatMessage is received.
	 * @param receivedChatMessage the received message.
	 * @throws SkypeException when a connection has gone bad.
	 */
	void chatMessageReceived(ChatMessage receivedChatMessage) throws SkypeException;
	
	/**
	 * This method is called when a chat message is sent.
	 * @param sentChatMessage the sent message.
	 * @throws SkypeException when a connection has gone bad.
	 */
    void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException;
}
