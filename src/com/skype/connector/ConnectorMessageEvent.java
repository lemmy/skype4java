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
 * Event object a connector will use when it fires a message received or sent event.
 */
public final class ConnectorMessageEvent extends ConnectorEvent {
    /**
	 * Needed for serialisation.
	 */
    private static final long serialVersionUID = -8610258526127376241L;
	
	/**
	 * The message that triggered the event.
	 */
	private final String message;

	/**
	 * Constructor with source (connector) and the message.
	 * @param source Connector which threw the event.
	 * @param newMessage The message sent or received.
	 */
    ConnectorMessageEvent(Object source, String newMessage) {
        super(source);
        assert newMessage != null;
        this.message = newMessage;
    }
    
    /**
     * Get the message of this event.
     * @return Message.
     */
    public String getMessage() {
        return message;
    }
}
