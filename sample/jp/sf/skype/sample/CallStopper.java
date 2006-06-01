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
package jp.sf.skype.sample;

import jp.sf.skype.Call;
import jp.sf.skype.CallAdapter;
import jp.sf.skype.Skype;
import jp.sf.skype.SkypeException;

public class CallStopper {
    public static void main(String[] args) throws Exception {
        Skype.setDeamon(false);
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callMaked(Call makedCall) {
                try {
                    makedCall.finish();
                } catch (SkypeException e) {
                }
            }
            @Override
            public void callReceived(Call receivedCall) {
                try {
                    receivedCall.finish();
                } catch (SkypeException e) {
                }
            }
        });
    }
}
