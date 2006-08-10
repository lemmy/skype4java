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
package com.skype.connector.test;

import com.skype.Call;
import com.skype.Skype;

public final class SampleTest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }
    
    public void testGetVersion() throws Exception {
        String result = Skype.getVersion();
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            assertEquals("2.5.0.130", result);
        }
    }
    
    public void testCall() throws Exception {
        Call call = Skype.getContactList().getFriend("echo123").call();
        Thread.sleep(5000);
        call.finish();

        String data = call.getDuration() + "," + call.getId() + "," + call.getPartnerId() + "," + call.getType();
        if (isRecordingMode()) {
            System.out.println(data);
        } else {
            assertEquals("2,8345,echo123,OUTGOING_P2P", data);
        }
    }
}
