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

public class ChatAPITest extends TestCase {
    public void testChat() throws Exception {
        Friend friend = Skype.getContactList().getFriend(TestData.getFriendId());
        Chat chat = friend.chat();
        chat.send("Test Message");
        TestUtils.showCheckDialog(TestData.getFriendId() + " has received \"Test Message\"ÅH");
        chat.setTopic("New Topic");
        TestUtils.showCheckDialog("Topic was changed to \"New Topic\"ÅH");
    }
}
