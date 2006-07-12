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
 * This exception is used for commands that get a ERROR reply.
 * @author Koji Hisano
 */
public final class CommandFailedException extends SkypeException {
    /**
	 * serialVersionUID needed for all serialisation objects.
	 */
	private static final long serialVersionUID = 5247715297475793607L;

	/**
	 * ERROR code refrence.
	 * @see https://developer.skype.com/Docs/ApiDoc/Error_codes
	 */
	private int code;

	/**
	 * The error message.
	 */
    private String message;

    /**
     * Constructor with parsing.
     * @param response the complete ERROR string.
     */
    CommandFailedException(String response) {
        super(response);
        if (response.startsWith("ERROR ")) {
            response = response.substring("ERROR ".length());
        }
        int spaceIndex = response.indexOf(' ');
        code = Integer.parseInt(response.substring(0, spaceIndex));
        message = response.substring(spaceIndex + 1);
    }

    /**
     * returns the error code.
     * @return error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the humanreadible error message.
     * @return message.
     */
    public String getMessage() {
        return message;
    }
}
