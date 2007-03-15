/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.skype.ContactList;
import com.skype.Friend;
import com.skype.Skype;

import junit.framework.TestCase;

public final class FriendAPITest extends TestCase {
    private TestCaseProperties data = new TestCaseProperties(getClass());

    public void testGetFriends() throws Exception {
        ContactList list = Skype.getContactList();
        Friend[] friend = list.getAllFriends();
        assertTrue(0 < friend.length);
    }

    public void testGetFriend() throws Exception {
        ContactList list = Skype.getContactList();
        Friend friend = list.getFriend(data.getProperty("id"));
        assertNotNull(friend);
    }

    public void testFriendProperties() throws Exception {
        Friend friend = Skype.getContactList().getFriend(data.getProperty("id"));
        assertEquals(TestData.getFriendDisplayName(), friend.getFullName());
        try {
            Date expectedBirthDay = new SimpleDateFormat("yyyy/MM/dd").parse(data.getProperty("birthDay"));
            assertEquals(expectedBirthDay, friend.getBirthDay());
        } catch (ParseException e) {
            fail("check FRIEND_BIRTHDAY constant value's format");
        }
        assertEquals(data.getProperty("sex"), "" + friend.getSex());
        assertEquals(data.getProperty("language"), friend.getLauguage());
        assertEquals(data.getProperty("country"), friend.getCountry());
        assertEquals(data.getProperty("province"), friend.getProvince());
        assertEquals(data.getProperty("city"), friend.getCity());
        assertEquals(data.getProperty("homePageAddress"), friend.getHomePageAddress());
        assertFalse(data.getProperty("isVideoCapable"), friend.isVideoCapable());
    }
}
