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

public class CallAPITest extends TestCase {
    public void testCallAndFinish() throws Exception {
        TestUtils.showMessageDialog(TestData.getFriendId() + "に発信が行われて2秒後に切れるかどうか確認してください");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(2000);
        call.finish();
        TestUtils.showCheckDialog(TestData.getFriendId() + "に発信が行われて2秒後に切れましたか？");
    }

    public void testFinishEndedCall() throws Exception {
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        call.finish();
        Thread.sleep(1000);
        try {
            call.finish();
        } catch (CommandFailedException e) {
            assertEquals(24, e.getCode());
            assertEquals("Cannot hangup inactive call", e.getMessage());
        }
    }

    public void testHoldAndResume() throws Exception {
        TestUtils.showMessageDialog(TestData.getFriendId() + "に発信が行われて10秒以内に会話を開始してください");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(10000);
        TestUtils.showMessageDialog("5秒間通話が中断された後に5秒間通話が再開されるか確認してください");
        call.hold();
        Thread.sleep(5000);
        call.resume();
        Thread.sleep(5000);
        call.finish();
        TestUtils.showCheckDialog("5秒間通話が中断された後に5秒間通話が再開されましたか？");
    }

    public void testCallProperty() throws Exception {
        Date startTime = new Date();
        TestUtils.showMessageDialog(TestData.getFriendId() + "に発信が行われて10秒以内に会話を開始してください");
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Call call = friend.call();
        Thread.sleep(10000);
        TestUtils.showMessageDialog("自動切断後に各種プロパティの値がテストされます");
        call.finish();
        Date endTime = new Date();
        assertTrue(call.getStartTime().getTime() - startTime.getTime() <= endTime.getTime() - startTime.getTime());
        assertTrue(call.getDuration() <= endTime.getTime() - startTime.getTime());
        assertEquals(TestData.getFriendId(), call.getPartnerId());
        assertEquals(TestData.getFriendDisplayName(), call.getPartnerDisplayName());
    }

    public void testCallReceived() throws Exception {
        final Call[] result = new Call[1];
        Skype.addCallReceivedListener(new CallReceivedListener() {
            public void callReceived(Call call) {
                result[0] = call;
            }
        });
        TestUtils.showMessageDialog("自分に対して" + TestData.getFriendId() + "に発信を依頼して着信したらダイアログを閉じて切断してください");
        assertEquals(TestData.getFriendId(), result[0].getPartnerId());
    }
}
