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

import com.skype.Friend;
import com.skype.Skype;
import com.skype.SkypeException;

final class TestData {
    private static TestCaseProperties data = new TestCaseProperties(TestData.class);

    static Friend getFriend() throws SkypeException {
        return Skype.getContactList().getFriend(data.getProperty("id"));
    }

    static Friend getFriend2() throws SkypeException {
        return Skype.getContactList().getFriend(data.getProperty("id2"));
    }

    static String getFriendId() throws SkypeException {
        return getFriend().getId();
    }

    static String getFriendDisplayName() throws SkypeException {
        return getFriend().getDisplayName();
    }

    private TestData() {
    }
}
