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
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import com.skype.Skype;

import junit.framework.TestCase;

public class SystemTest extends TestCase {
    public void testIsInstalled() throws Exception {
        assertTrue(Skype.isInstalled());
    }

    public void testGetInstalledPath() throws Exception {
        assertEquals("C:\\Program Files\\Skype\\Phone\\Skype.exe", Skype.getInstalledPath());
    }

    public void testIsRunning() throws Exception {
        assertTrue(Skype.isRunning());
    }

    public void testGetVersion() throws Exception {
        String version = Skype.getVersion();
        assertNotNull(version);
        assertTrue(!"".equals(version));
    }
}
