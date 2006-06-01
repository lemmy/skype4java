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
package jp.sf.skype;

import junit.framework.TestCase;

public class CallListenerTest extends TestCase {
    public void testBasic() throws Exception {
        Skype.setDebug(true);
        final boolean[] maked = new boolean[1];
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callMaked(Call makedCall) {
                maked[0] = true;
                Skype.removeCallListener(this);
            }
        });
        TestUtils.showMessageDialog("Please, make a call to " + TestData.getFriendId() + " and finish.");
        assertTrue(maked[0]);

        final boolean[] received = new boolean[1];
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callReceived(Call receivedCall) {
                received[0] = true;
                Skype.removeCallListener(this);
            }
        });
        TestUtils.showMessageDialog("Please, receive a call from " + TestData.getFriendId() + " and finish.");
        assertTrue(received[0]);
    }
}
