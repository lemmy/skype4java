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

public class AutoChatAPITest extends TestCaseByCSVFile {
    @Override
    protected void setUp() throws Exception {
        setRecordingMode(false);
    }
    
    public void testGetAllChat() throws Exception {
        Chat[] chats = Skype.getAllChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_CHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllActiveChat() throws Exception {
        Chat[] chats = Skype.getAllActiveChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_ACTIVECHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllMissedChat() throws Exception {
        Chat[] chats = Skype.getAllMissedChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            //data from https://developer.skype.com/Docs/ApiDoc/SEARCH_MISSEDCHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllRecentChat() throws Exception {
        Chat[] chats = Skype.getAllRecentChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_RECENTCHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }
    
    public void testGetAllBookmarkedChat() throws Exception {
        Chat[] chats = Skype.getAllBookmarkedChats();
        String result = toString(chats);
        if (isRecordingMode()) {
            System.out.println(result);
        } else {
            // data from https://developer.skype.com/Docs/ApiDoc/SEARCH_BOOKMARKEDCHATS
            assertEquals("#bitman/$jessy;eb06e65612353279, #bitman/$jdenton;9244e98f82d7d391", result);
        }
    }

    private String toString(Chat[] chats) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, length = chats.length; i < length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(chats[i].getId());
        }
        return builder.toString();
    }
}
