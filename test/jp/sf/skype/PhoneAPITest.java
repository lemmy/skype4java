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

import junit.framework.TestCase;

public final class PhoneAPITest extends TestCase {
    public void testAudioInputDevice() throws Exception {
        String device = Skype.getAudioInputDevice();
        Skype.setAudioInputDevice(null);
        assertNull(Skype.getAudioInputDevice());
        Skype.setAudioInputDevice(device);
        assertEquals(device, Skype.getAudioInputDevice());
    }

    public void testAudioOutputDevice() throws Exception {
        String device = Skype.getAudioOutputDevice();
        Skype.setAudioOutputDevice(null);
        assertNull(Skype.getAudioOutputDevice());
        Skype.setAudioOutputDevice(device);
        assertEquals(device, Skype.getAudioOutputDevice());
    }
}