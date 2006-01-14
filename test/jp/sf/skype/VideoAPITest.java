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
            name = "標準ビデオデバイス";
        }
        TestUtils.showCheckDialog("Webカメラは[" + name + "]に設定されていますか？");
        Skype.setVideoDevice("");
    }

    public void testOpenVideoTestWindow() throws Exception {
        Skype.openVideoTestWindow();
        TestUtils.showCheckDialog("Webカメラのテストウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にWebカメラのテストウィンドウを閉じてください");
    }

    public void testOpenVideoOptionsWindow() throws Exception {
        Skype.openVideoOptionsWindow();
        TestUtils.showCheckDialog("ビデオ設定ページが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にビデオ設定ページのウィンドウを閉じてください");
    }
}
