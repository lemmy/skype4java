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
 * Koji Hisano - initial API, implementation and changed javadoc
 * Bart Lamot - initial javadocs
 ******************************************************************************/
package com.skype;

/**
 * Listener interface for VOICEMAIL objects status changed events.
 * @author Koji Hisano.
 */
public interface VoiceMailStatusChangedListener {
    /**
     * Called when the status of a VOICEMAIL object changes.
     * @param status the new status.
     * @throws SkypeException when a connection is gone bad.
     */
	void statusChanged(VoiceMail.Status status) throws SkypeException;
}
