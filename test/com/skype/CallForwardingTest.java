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

import com.skype.Skype;
import com.skype.Profile.CallForwardingRule;
import com.skype.Profile.Status;

import junit.framework.TestCase;

public class CallForwardingTest extends TestCase {
    public void testSetCallForwarding() throws Exception {
        Skype.setDeamon(true);
        Skype.setDebug(true);
        Skype.getProfile().setStatus(Status.OFFLINE);
        // CallForwardingRule[] oldCallForwardingRules =
        // Skype.getProfile().getAllCallForwardingRules();
        // boolean oldCallForwarding = Skype.getProfile().isCallForwarding();
        Skype.getProfile().setAllCallForwardingRules(new CallForwardingRule[] { new CallForwardingRule(0, 30, "+819018875000") });
        Skype.getProfile().setCallForwarding(true);
    }
}
