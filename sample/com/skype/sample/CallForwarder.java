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
package com.skype.sample;

import com.skype.Call;
import com.skype.CallAdapter;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.Profile.CallForwardingRule;

public class CallForwarder {
    public static void main(String[] args) throws Exception {
        Skype.setDeamon(false);
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callReceived(Call receivedCall) throws SkypeException {
                CallForwardingRule[] oldRules = Skype.getProfile().getAllCallForwardingRules();
                Skype.getProfile().setAllCallForwardingRules(new CallForwardingRule[] { new CallForwardingRule(0, 30, "echo123") });
                receivedCall.forward();
                try {
                    Thread.sleep(10000); // to prevent finishing this call
                } catch (InterruptedException e) {
                }
                Skype.getProfile().setAllCallForwardingRules(oldRules);
            }
        });
    }
}
