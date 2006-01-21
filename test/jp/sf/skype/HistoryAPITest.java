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

import java.util.Date;
import junit.framework.TestCase;

public class HistoryAPITest extends TestCase {
    public void testGetAllMessages() throws Exception {
        TestUtils.showMessageDialog(TestData.getFriendId() + "にチャットメッセージ「テスト」を送信後、10秒以内にこのダイアログを閉じてください");
        Friend friend = TestData.getFriend();
        Message[] messages = friend.getAllMessages();
        assertTrue(0 < messages.length);
        Message latest = messages[0];
        // TODO PartnerIdなのに自分のIdが返ってくる(Skypeのバグ?)
        assertEquals(Skype.getProfile().getId(), latest.getPartnerId());
        // TODO Invalid PROPとエラーが返ってくる(Skypeのバグ?)
//      assertEquals(Skype.getProfile().getDisplayName(), latest.getPartnerDisplayName());
        assertTrue(new Date().getTime() - 10000 <= latest.getStartTime().getTime());
        assertEquals(Message.Type.TEXT, latest.getType());
        assertEquals("テスト", latest.getMessage());
    }

    public void testGetAllCalls() throws Exception {
        TestUtils.showMessageDialog(TestData.getFriendId() + "に発信して発信して切断後、10秒以内にこのダイアログを閉じてください");
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
