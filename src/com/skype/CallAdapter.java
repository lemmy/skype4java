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
 * Empty implementation of CallListener to overide and use as a listener.
 * @author Koji Hisano
 *
 */
public class CallAdapter implements CallListener {
	/**
	 * This method will be triggered when a CALL is received.
	 * @param receivedCall the CALL received.
	 * @throws SkypeException when connection is gone bad.
	 */
    public void callReceived(Call receivedCall) throws SkypeException {
    }

    /**
     * This method is called when a new CALL is started.
     * @param makedCall the new CALL made.
     * @throws SkypeException when the connection is goen bad.
     */
    public void callMaked(Call makedCall) throws SkypeException {
    }
}
