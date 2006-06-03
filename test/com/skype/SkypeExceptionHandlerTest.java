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

public final class SkypeExceptionHandlerTest extends TestCase {
    public void testDefaultHanlder() throws Exception {
        fireHanlderWithSkypeException();
        TestUtils.showCheckDialog("The default output contains a stack trace?");
    }

    public void testSetHandler() throws Exception {
        final Object wait = new Object();
        final boolean[] result = new boolean[1];
        Skype.setSkypeExceptionHandler(new SkypeExceptionHandler() {
            public void uncaughtExceptionHappened(SkypeException e) {
                result[0] = true;
                synchronized (wait) {
                    wait.notify();
                }
            }
        });
        fireHanlderWithSkypeException();
        synchronized (wait) {
            try {
                wait.wait();
            } catch (InterruptedException e) {
            }
        }
        assertTrue(result[0]);
        
        Skype.setSkypeExceptionHandler(null);
        fireHanlderWithSkypeException();
        TestUtils.showCheckDialog("The default output contains a stack trace?");
    }

    private void fireHanlderWithSkypeException() throws SkypeException {
        final Object wait = new Object();
        ChatMessageListener listener = new ChatMessageAdapter() {
            @Override
            public void chatMessageSent(ChatMessage sentChatMessage) throws SkypeException {
                try {
                    throw new SkypeException();
                } finally {
                    synchronized (wait) {
                        wait.notify();
                    }
                }
            }
        };
        Skype.addChatMessageListener(listener);
        TestData.getFriend().send("a message for a method test");
        synchronized (wait) {
            try {
                wait.wait();
            } catch (InterruptedException e) {
            }
        }
        Skype.removeChatMessageListener(listener);
    }
}
