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
 * Exception to throw when a timeout occurs.
 */
public class TimeOutException extends SkypeException {
    /**
	 * serialVersionUID needed for serialisation.
	 */
    private static final long serialVersionUID = -5760422025501667771L;

	/**
	 * Constructor.
	 * @param message exception message.
	 */
	TimeOutException(String message) {
        super(message);
    }
}
