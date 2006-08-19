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

import com.skype.connector.test.TestCaseByCSVFile;

public class AutoVoiceMailAPITest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }
    
    public void testGetAllVoiceMails() throws Exception {
        VoiceMail[] voiceMails = Skype.getAllVoiceMails();
        String result = toString(voiceMails);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_VOICEMAILS
            assertEquals("65, 70, 71", result);
        }
    }
    
    private String toString(VoiceMail[] voiceMails) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, length = voiceMails.length; i < length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(voiceMails[i].getId());
        }
        return builder.toString();
    }
    
    public void testVoiceMail() throws Exception {
        VoiceMail voiceMail = Skype.voiceMail("echo123");
        String result = voiceMail.getId() + ", " + voiceMail.getType() + ", " + voiceMail.getPartnerId() + ", " + voiceMail.getPartnerDisplayName() + ", " + voiceMail.getStartTime().getTime() + ", " + voiceMail.getDuration() + ", " + voiceMail.getAllowedDuration();
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            assertEquals("2346, OUTGOING, echo123, Skype Test Call, 1156016035000, 0, 600", result);
        }
    }
}