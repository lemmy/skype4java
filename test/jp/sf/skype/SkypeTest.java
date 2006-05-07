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

public class SkypeTest extends TestCase {
    public void testMessageReceived() throws Exception {
        final Object lock = new Object();
        final ChatMessage[] result = new ChatMessage[1];
        Skype.addChatMessageReceivedListener(new ChatMessageReceivedListener() {
            public void chatMessageReceived(ChatMessage message) {
                result[0] = message;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        TestUtils.showMessageDialog("このダイアログを閉じてから1分以内に" + TestData.getFriendId() + "から'test'とメッセージを送ってもらってください");
        synchronized (lock) {
            try {
                lock.wait(60000);
            } catch (InterruptedException e) {
            }
        }
        ChatMessage message = result[0];
        assertEquals(TestData.getFriendId(), message.getSenderId());
        assertEquals(TestData.getFriendDisplayName(), message.getSenderDisplayName());
        assertEquals("test", message.getContent());
    }
}
