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
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import com.skype.Call.DTMF;
import com.skype.connector.test.TestCaseByCSVFile;

public class AutoCallAPITest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }
    
//    public void testConferenceId() throws Exception {
//        Call call = Skype.call("bitman", "jessy");
//        String result = call.getConferenceId();
//        if (isRecordingMode()) {
//            System.out.println(result);
//        } else {
//            assertEquals("11676", result);
//        }
//    }
    
    public void testSendDTMF() throws Exception {
        Thread.sleep(2000);
        Call call = Skype.call("echo123");
        for (DTMF command: DTMF.values()) {
            call.send(command);
        }
        call.finish();
    }
}
