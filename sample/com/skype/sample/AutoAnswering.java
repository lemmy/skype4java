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
package com.skype.sample;

import com.skype.ChatMessage;
import com.skype.ChatMessageAdapter;
import com.skype.Skype;
import com.skype.SkypeException;

public class AutoAnswering {
    public static void main(String[] args) throws Exception {
        Skype.setDeamon(false); // to prevent exiting from this program
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            public void chatMessageReceived(ChatMessage received) throws SkypeException {
                if (received.getType().equals(ChatMessage.Type.SAID)) {
                    received.getSender().send("I'm working. Please, wait a moment.");
                }
            }
        });
    }
}
