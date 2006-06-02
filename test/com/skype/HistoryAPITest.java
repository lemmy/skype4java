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

import java.util.Date;

import com.skype.Call;
import com.skype.ChatMessage;
import com.skype.Friend;

import junit.framework.TestCase;

public class HistoryAPITest extends TestCase {
    public void testGetAllMessages() throws Exception {
        TestUtils.showMessageDialog("Please, send a chat message 'Hello, World!' to " + TestData.getFriendId() + ".");
        Friend friend = TestData.getFriend();
        ChatMessage[] messages = friend.getAllChatMessages();
        assertTrue(0 < messages.length);
    }

    public void testGetAllCalls() throws Exception {
        TestUtils.showMessageDialog("Please, start a call to " + TestData.getFriendId() + "and finsh it in 10 seconds.");
        Friend friend = TestData.getFriend();
        Call[] calls = friend.getAllCalls();
        assertTrue(0 < calls.length);
        Call latest = calls[0];
        assertEquals(TestData.getFriendId(), latest.getPartnerId());
        assertEquals(TestData.getFriendDisplayName(), latest.getPartnerDisplayName());
        assertTrue(new Date().getTime() - 10000 <= latest.getStartTime().getTime());
        assertEquals(Call.Type.OUTGOING_P2P, latest.getType());
    }
}
