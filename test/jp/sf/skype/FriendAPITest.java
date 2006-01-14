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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
