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
 * Exception class which connectors can throw.
 */
public class ConnectorException extends Exception {
	private static final long serialVersionUID = -764987191989792842L;

    /**
     * Constructor.
     */
	public ConnectorException() {
    }

	/**
	 * Constructor with message.
	 * @param message The exception message.
	 */
    public ConnectorException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     * @param message The exception message.
     * @param cause The cause exception.
     */
    public ConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
