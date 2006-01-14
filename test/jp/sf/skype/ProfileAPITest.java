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

import java.text.SimpleDateFormat;
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
