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
 * Empty implementation of VoiceMailListener to overide and use as a listener.
 * @author Koji Hisano
 */
public class AbstractVoiceMailListener implements VoiceMailListener {
    /**
     * Called when a new voice mail is received.
     * @param receivedVoiceMail the received voice mail
     * @throws SkypeException if a connection is bad
     */
    public void voiceMailReceived(VoiceMail receivedVoiceMail) throws SkypeException {
    }

    /**
     * Called when a new voice mail is made.
     * @param madeVoiceMail the made voice mail
     * @throws SkypeException if the connection is bad.
     */
    public void voiceMailMade(VoiceMail madeVoiceMail) throws SkypeException {
    }
}
