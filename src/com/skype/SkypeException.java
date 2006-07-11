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
 ******************************************************************************/
package com.skype;

/**
 * Exception that will be thrown when the connection with Skype is not OK or unexpected events happen.
 * @author Koji Hisano
 *
 */
public class SkypeException extends Exception {
    /**
	 * SerialVersionUID needed for serialisation.
	 */
	private static final long serialVersionUID = -4277557764382543108L;

	/**
	 * Constructor.
	 *
	 */
	SkypeException() {
    }

	/**
	 * Constructor with message to provide with the exception.
	 * @param message the exception message.
	 */
    SkypeException(String message) {
        super(message);
    }

    /**
     * Constructor not only with a message but also a cause.
     * @param message the exception message.
     * @param cause the exception cause.
     */
    SkypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
