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

import jp.sf.skype.Skype;
import junit.framework.TestCase;

public class SystemTest extends TestCase {
    public void testIsRunning() throws Exception {
        assertTrue(Skype.isRunning());
    }

    public void testGetVersion() throws Exception {
        String version = Skype.getVersion();
        assertNotNull(version);
        assertTrue(!"".equals(version));
    }
}
