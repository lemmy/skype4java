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
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.text.SimpleDateFormat;

import com.skype.Profile;
import com.skype.Skype;

import junit.framework.TestCase;

public final class ProfileAPITest extends TestCase {
    private TestCaseProperties data = new TestCaseProperties(getClass());

    public void testFriendProperties() throws Exception {
        Profile profile = Skype.getProfile();
        assertEquals(data.getProperty("id"), profile.getId());
        assertEquals(Profile.Status.ONLINE, profile.getStatus());
        assertEquals(data.getProperty("canDoSkypeOut"), "" + profile.canDoSkypeOut());
        assertEquals(data.getProperty("canDoSkypeIn"), "" + profile.canDoSkypeIn());
        assertEquals(data.getProperty("canDoVoiceMail"), "" + profile.canDoVoiceMail());
        assertTrue(0 <= profile.getPSTNBalance());
        assertEquals(data.getProperty("pstnBalanceCurrencyUnit"), profile.getPSTNBalanceCurrencyUnit());
        assertEquals(data.getProperty("fullName"), profile.getFullName());
        assertEquals(data.getProperty("birthDay"), "" + new SimpleDateFormat("yyyy/MM/dd").format(profile.getBirthDay()));
        assertEquals(data.getProperty("sex"), "" + profile.getSex());
        assertEquals(data.getProperty("language"), profile.getAllLauguages()[0]);
        assertEquals(data.getProperty("countryByISOCode"), profile.getCountryByISOCode());
        assertEquals(data.getProperty("country"), profile.getCountry());
        assertEquals(data.getProperty("province"), profile.getProvince());
        assertEquals(data.getProperty("city"), profile.getCity());
        assertEquals(data.getProperty("homePhoneNumber"), profile.getHomePhoneNumber());
        assertEquals(data.getProperty("officePhoneNumber"), profile.getOfficePhoneNumber());
        assertEquals(data.getProperty("mobilePhoneNumber"), profile.getMobilePhoneNumber());
        assertEquals(data.getProperty("homePageAddress"), profile.getHomePageAddress());
        assertEquals(data.getProperty("introduction"), profile.getIntroduction());
        assertEquals(data.getProperty("moodMessage"), profile.getMoodMessage());
        assertEquals(data.getProperty("timeZone"), "" + profile.getTimeZone());
    }
}
