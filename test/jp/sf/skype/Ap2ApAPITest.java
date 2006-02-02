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

public class Ap2ApAPITest extends TestCase {
    public void testApplication() throws Exception {
        Skype.setDebug(true);
        Application application = Skype.addApplication("AP2AP");
        Friend friend = TestData.getFriend();
        checkConnectableFriendsBeforeConnecting(application);
        try {
            Stream stream = application.connect(friend);
            checkConnectableFriendsAfterConnecting(application);
            checkConnectedFriends(application, friend);
            checkWrite(stream);
            checkSend(stream);
            checkDisconnect(application, stream);
        } finally {
            application.finish();
        }
    }

    private void checkConnectableFriendsBeforeConnecting(Application application) throws SkypeException {
        Friend[] connectableFriends = application.getAllConnectableFriends();
        assertTrue(1 <= connectableFriends.length);
    }

    private void checkConnectableFriendsAfterConnecting(Application application) throws SkypeException {
        Friend[] connectableFriends = application.getAllConnectableFriends();
        assertTrue(0 <= connectableFriends.length);
    }

    private void checkConnectedFriends(Application application, Friend friend) throws SkypeException {
        Friend[] connectableFriends = application.getAllConnectedFriends();
        assertEquals(1, connectableFriends.length);
        assertEquals(friend, connectableFriends[0]);
    }

    private void checkWrite(Stream stream) throws Exception {
        final Object lock = new Object();
        final String[] result = new String[1];
        stream.addStreamListener(new StreamAdapter() {
            @Override
            public void textReceived(String text) {
                result[0] = text;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            stream.write("Hello, World!");
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                fail();
            }
        }
        assertEquals("Hello, World!", result[0]);
    }

    private void checkSend(Stream stream) throws Exception {
        final Object lock = new Object();
        final String[] result = new String[1];
        stream.addStreamListener(new StreamAdapter() {
            @Override
            public void datagramReceived(String datagram) {
                result[0] = datagram;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            stream.send("Hello, World!");
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                fail();
            }
        }
        assertEquals("Hello, World!", result[0]);
    }

    private void checkDisconnect(Application application, Stream stream) throws Exception {
        final Object lock = new Object();
        application.addApplicationListener(new ApplicationAdapter() {
            public void disconnected(Stream stream) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            stream.write("disconnect");
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                fail();
            }
        }
    }
}
