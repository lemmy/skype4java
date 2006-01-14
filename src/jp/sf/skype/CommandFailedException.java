/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

public final class CommandFailedException extends SkypeException {
    private int code;
    private String message;

    CommandFailedException(String response) {
        super(response);
        if (response.startsWith("ERROR ")) {
            response = response.substring("ERROR ".length());
        }
        int spaceIndex = response.indexOf(' ');
        code = Integer.parseInt(response.substring(0, spaceIndex));
        message = response.substring(spaceIndex + 1);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
