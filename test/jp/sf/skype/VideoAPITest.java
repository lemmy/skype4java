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

public class VideoAPITest extends TestCase {
    public void testGetVideoDevice() throws Exception {
        String name = Skype.getVideoDevice();
        if (name == null) {
            name = "Default video device";
        }
        TestUtils.showCheckDialog("Webcam is '" + name + "'?");
    }

    public void testOpenVideoTestWindow() throws Exception {
        Skype.openVideoTestWindow();
        TestUtils.showCheckDialog("Webcam test window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testOpenVideoOptionsWindow() throws Exception {
        Skype.openVideoOptionsWindow();
        TestUtils.showCheckDialog("Options window with selectiong 'Video' page is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }
}
