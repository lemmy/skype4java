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

public final class Ap2ApAPIStressTestServer {
    public static void main(String[] args) throws Exception {
        Skype.setDebug(true);
        final Application application = Skype.addApplication(Ap2ApAPIStressTest.APPLICATION_NAME);
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
