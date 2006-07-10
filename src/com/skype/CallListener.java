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
 * Listener interface for the CALL object.
 * @author Koji Hisano.
 *
 */
public interface CallListener {
    /**
     * Called when a CALL is received.
     * @param receivedCall the received CALL.
     * @throws SkypeException when a connection is gone bad.
     */
	void callReceived(Call receivedCall) throws SkypeException;
	
	/**
	 * Called when a new CALL is started.
	 * @param makedCall the new CALL made.
	 * @throws SkypeException when the connection is goen bad.
	 */
    void callMaked(Call makedCall) throws SkypeException;
}
