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
package jp.sf.skype.sample;

import jp.sf.skype.ChatMessage;
import jp.sf.skype.ChatMessageAdapter;
import jp.sf.skype.Skype;
import jp.sf.skype.SkypeException;

public class AutoAnswering {
    public static void main(String[] args) throws Exception {
        Skype.setDeamon(false); // to prevent exiting from this program
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            public void chatMessageReceived(ChatMessage received) {
                try {
                    if (received.getType().equals(ChatMessage.Type.SAID)) {
                        received.getSender().send("I'm working. Please, wait a moment.");
                    }
                } catch (SkypeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
