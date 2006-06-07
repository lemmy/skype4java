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

import junit.framework.TestCase;

public class ChatAPITest extends TestCase {
    public void testChat() throws Exception {
        Friend friend = TestData.getFriend();
        Chat chat = friend.chat();
        chat.send("Test Message");
        TestUtils.showCheckDialog(TestData.getFriendId() + " has received \"Test Message\"？");
        chat.setTopic("New Topic");
        TestUtils.showCheckDialog("Topic was changed to \"New Topic\"？");
        Friend friend2 = TestData.getFriend2();
        chat.addUser(friend2);
        TestUtils.showCheckDialog(friend2.getId() + " was added to this chat？");
        chat.leave();
        TestUtils.showCheckDialog("You have left from this chat？");
        assertTrue(0 < chat.getAllChatMessages().length);
        assertTrue(0 < chat.getRecentChatMessages().length);
    }
}
