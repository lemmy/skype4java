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

import com.skype.Application;
import com.skype.ApplicationAdapter;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.Stream;
import com.skype.StreamAdapter;

public final class Ap2ApAPITestServer {
    public static void main(String[] args) throws Exception {
        Skype.setDebug(true);
        final Application application = Skype.addApplication(Ap2ApAPITest.APPLICATION_NAME);
        final Object lock = new Object();
        application.addApplicationListener(new ApplicationAdapter() {
            public void connected(final Stream stream) {
                stream.addStreamListener(new StreamAdapter() {
                    @Override
                    public void textReceived(String text) {
                        try {
                            if ("disconnect".equals(text)) {
                                stream.disconnect();
                                return;
                            }
                            stream.write(text);
                        } catch (SkypeException e) {
                            synchronized (lock) {
                                lock.notify();
                            }
                            System.err.println("couldn't respond to " + stream.getFriend().getId() + " text");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void datagramReceived(String datagram) {
                        try {
                            stream.send(datagram);
                        } catch (SkypeException e) {
                            synchronized (lock) {
                                lock.notify();
                            }
                            System.err.println("couldn't respond to " + stream.getFriend().getId() + " datagram");
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void disconnected(Stream stream) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
            }
        }
        application.finish();
    }
}
