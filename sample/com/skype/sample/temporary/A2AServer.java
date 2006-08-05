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
package com.skype.sample.temporary;

import com.skype.Application;
import com.skype.ApplicationAdapter;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.Stream;
import com.skype.StreamAdapter;

public class A2AServer {
    public static void main(String[] args) throws Exception {
        Skype.setDebug(true);
        Skype.setDeamon(false);
        Application application = Skype.addApplication(A2AClient.class.getName());
        application.addApplicationListener(new ApplicationAdapter() {
            @Override
            public void connected(Stream stream) throws SkypeException {
                System.out.println("connected:" + stream.getId());
                stream.addStreamListener(new StreamAdapter() {
                    @Override
                    public void textReceived(String receivedText) throws SkypeException {
                        System.out.println("received:" + Integer.toHexString(receivedText.length()));
                    }
                });
            }
        });
    }
}
