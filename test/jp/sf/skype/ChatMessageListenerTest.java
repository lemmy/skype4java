/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
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

public class ChatMessageListenerTest extends TestCase {
    public void testBasic() throws Exception {
        final boolean[] sent = new boolean[1];
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            @Override
            public void chatMessageSent(ChatMessage sentChatMessage) {
                sent[0] = true;
                Skype.removeChatMessageListener(this);
            }
        });
        TestUtils.showMessageDialog("Please, send a chat message to " + TestData.getFriendId() + ".");
        assertTrue(sent[0]);

        final boolean[] received = new boolean[1];
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            @Override
            public void chatMessageReceived(ChatMessage receivedChatMessage) {
                received[0] = true;
                Skype.removeChatMessageListener(this);
            }
        });
        TestUtils.showMessageDialog("Please, receive a chat message from " + TestData.getFriendId() + ".");
        assertTrue(received[0]);
    }
}
