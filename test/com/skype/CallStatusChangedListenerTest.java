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
package com.skype;

import com.skype.Call;
import com.skype.CallAdapter;
import com.skype.Skype;
import com.skype.Call.Status;

import junit.framework.TestCase;

public class CallStatusChangedListenerTest extends TestCase {
    public void testBasic() throws Exception {
        final StringBuffer statuses = new StringBuffer();
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callMaked(Call makedCall) throws SkypeException {
                makedCall.addCallStatusChangedListener(new CallStatusChangedListener() {
                    public void statusChanged(Status status) throws SkypeException {
                        statuses.append("[" + status + "]");
                    }
                });
            }
        });
        TestUtils.showMessageDialog("Please, make a call to " + TestData.getFriendId() + " and finish.");
        assertEquals("[RINGING][CANCELLED]", statuses.toString());
    }
}
