/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The <code>Profile</code> class contains the current user's information.
 * <p>
 * For example, you can get the mood message of the current user by this code:
 * <pre>String moodMessage = Skype.getProfile().getMoodMessage();</pre>
 * And you can change it by the following code:
 * <pre>Skype.getProfile().setMoodMessage("Happy!");</pre>
 * </p>
 */
public final class Profile {
    /**
     * The <code>Status</code> enum contains the online status constants of the current user.
     * @see Profile#getStatus()
     * @see Profile#setStatus(Status)
     */
    public enum Status {
        /**
         * The <code>UNKNOWN</code> constant indicates the user status is unknown.
         */
        UNKNOWN,
        /**
         * The <code>ONLINE</code> constant indicates the user is online.
         */
        ONLINE,
        /**
         * The <code>OFFLINE</code> constant indicates the user is offline.
         */
        OFFLINE,
        /**
         * The <code>SKYPEME</code> constant indicates the user is in SkypeMe mode.
         */
        SKYPEME,
        /**
         * The <code>AWAY</code> constant indicates the user is away.
         */
        AWAY,
        /**
         * The <code>NA</code> constant indicates the user is not available.
         */
        NA,
        /**
         * The <code>DND</code> constant indicates the user is in do not disturb mode.
         */
        DND,
        /**
         * The <code>INVISIBLE</code> constant indicates the user is invisible to others.
         */
        INVISIBLE,
        /**
         * The <code>LOGGEDOUT</code> constant indicates the user is logged out.
         */
        LOGGEDOUT;
    }

    /**
     * The <code>Sex</code> enum contains the sex constants of the current user.
     * @see Profile#getSex()
     * @see Profile#setSex(Sex)
     */
    public enum Sex {
        /**
         * The <code>UNKNOWN</code> constant indicates the sex of the current user is unknown.
         */
        UNKNOWN,
        /**
         * The <code>MALE</code> constant indicates the current user is male.
         */
        MALE,
        /**
         * The <code>FEMALE</code> constant indicates the current user is female.
         */
        FEMALE;
    }

    /**
     * The <code>CallForwardingRule</code> class contains the information of a call forwarding rule.
     */
    public static final class CallForwardingRule {
        private final int startSecond;
        private final int endSecond;
        private final String target;

        /**
         * Constructs a call forwarding rule.
         * @param startSecond the time in seconds when connecting to this number/user starts.
         * @param endSecond the time in seconds when ringing to this number/user ends.
         * @param target the target Skype username to forward calls to, or the PSTN number to forward a call.
         */
        public CallForwardingRule(int startSecond, int endSecond, String target) {
            this.startSecond = startSecond;
            this.endSecond = endSecond;
            if (target.startsWith("+")) {
                target = target.replaceAll("-", "");
            }
            this.target = target;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object compared) {
            if (compared instanceof CallForwardingRule) {
                return toString().equals(((Profile)compared).toString());
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return startSecond + "," + endSecond + "," + target;
        }

        /**
         * Gets the time in seconds when connecting to this number/user starts.
         * @return the time in seconds when connecting to this number/user starts.
         */
        public int getStartSecond() {
            return startSecond;
        }

        /**
         * Gets the time in seconds when ringing to this number/user ends.
         * @return the time in seconds when ringing to this number/user ends.
         */
        public int getEndSecond() {
            return endSecond;
        }

        /**
         * Gets the target Skype username to forward calls to, or the PSTN number to forward a call.
         * @return the target Skype username to forward calls to, or the PSTN number to forward a call.
         */
        public String getTarget() {
            return target;
        }
    }

    Profile() {
    }

    /**
     * Gets the Skype ID (username) of the current user. 
     * @return the Skype ID (username) of the current user.
     * @throws SkypeException
     */
    public String getId() throws SkypeException {
        return Utils.getProperty("CURRENTUSERHANDLE");
    }

    /**
     * Gets the online status of the current user.
     * @return the online status of the current user.
     * @throws SkypeException
     * @see #setStatus(Status)
     */
    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getProperty("USERSTATUS"));
    }

    /**
     * Sets the online status of the current user by the {@link Status} enum.
     * @param newStatus the new online status of the current user.
     * @throws SkypeException
     * @see #getStatus()
     */
    public void setStatus(Status newStatus) throws SkypeException {
        Utils.setProperty("USERSTATUS", newStatus.toString());
    }

    /**
     * Indicates whether the current user can do SkypeOut.
     * @return <code>true</code> if the current user can do SkypeOut; <code>false</code> otherwise.
     * @throws SkypeException
     */
    public boolean canDoSkypeOut() throws SkypeException {
        return canDo("SKYPEOUT");
    }

    /**
     * Indicates whether the current user can do SkypeIn.
     * @return <code>true</code> if the current user can do SkypeIn; <code>false</code> otherwise.
     * @throws SkypeException
     */
    public boolean canDoSkypeIn() throws SkypeException {
        return canDo("SKYPEIN");
    }

    /**
     * Indicates whether the current user can do VoiceMail.
     * @return <code>true</code> if the current user can do VoiceMail; <code>false</code> otherwise.
     * @throws SkypeException
     */
    public boolean canDoVoiceMail() throws SkypeException {
        return canDo("VOICEMAIL");
    }

    private boolean canDo(String name) throws SkypeException {
        return Boolean.parseBoolean(Utils.getProperty("PRIVILEGE", name));
    }

    /**
     * Gets the balance of the current user.
     * @return the balance of the current user.
     * @throws SkypeException
     */
    public int getPSTNBalance() throws SkypeException {
        return Integer.parseInt(getProperty("PSTN_BALANCE"));
    }

    /**
     * Gets the currency code of the current user.
     * @return the currency code of the current user.
     * @throws SkypeException
     */
    public String getPSTNBalanceCurrencyUnit() throws SkypeException {
        return getProperty("PSTN_BALANCE_CURRENCY");
    }

    /**
     * Gets the full name of the current user.
     * @return the full name of the current user.
     * @throws SkypeException
     * @see #setFullName(String)
     */
    public String getFullName() throws SkypeException {
        return getProperty("FULLNAME");
    }

    /**
     * Sets the full name of the current user.
     * @param newValue the new full name of the current user.
     * @throws SkypeException
     * @see #getFullName()
     */
    public void setFullName(String newValue) throws SkypeException {
        setProperty("FULLNAME", newValue);
    }

    /**
     * Gets the birth day of the current user.
     * @return the birth day of the current user.
     * @throws SkypeException
     * @see #setBirthDay(Date)
     */
    public Date getBirthDay() throws SkypeException {
        String value = getProperty("BIRTHDAY");
        if ("0".equals(value)) {
            return null;
        } else {
            try {
                return new SimpleDateFormat("yyyyMMdd").parse(value);
            } catch (ParseException e) {
                throw new IllegalStateException("library developer should check Skype specification.");
            }
        }
    }

    /**
     * Sets the birth day of the current user.
     * @param newValue the new birth day of the current user.
     * @throws SkypeException
     * @see #getBirthDay()
     */
    public void setBirthDay(Date newValue) throws SkypeException {
        String newValueString;
        if (newValue == null) {
            newValueString = "0";
        } else {
            newValueString = new SimpleDateFormat("yyyyMMdd").format(newValue);
        }
        setProperty("BIRTHDAY", newValueString);
    }

    /**
     * Gets the sex of the current user.
     * @return the sex of the current user.
     * @throws SkypeException
     * @see #setSex(Sex)
     */
    public Sex getSex() throws SkypeException {
        return Sex.valueOf((getProperty("SEX")));
    }

    /**
     * Sets the sex of the current user by the {@link Sex} enum.
     * @param newValue the new sex of the current user.
     * @throws SkypeException
     * @see #getSex()
     */
    public void setSex(Sex newValue) throws SkypeException {
        setProperty("SEX", newValue.toString());
    }

    /**
     * Gets the all languages of the current user.
     * @return the all languages of the current user.
     * @throws SkypeException
     * @see #setAllLanguages(String[])
     */
    public String[] getAllLauguages() throws SkypeException {
        return getProperty("LANGUAGES").split(" ");
    }

    /**
     * Sets the all languages of the current user.
     * @param newValues the all new languages of the current user.
     * @throws SkypeException
     * @see #getAllLauguages()
     */
    public void setAllLanguages(String[] newValues) throws SkypeException {
        if (newValues == null) {
            newValues = new String[0];
        }
        setProperty("LANGUAGES", toSpaceSeparatedString(newValues));
    }

    private String toSpaceSeparatedString(Object[] newValues) {
        StringBuilder newValuesString = new StringBuilder();
        for (Object newValue : newValues) {
            newValuesString.append(newValue);
        }
        return newValuesString.toString();
    }

    /**
     * Gets the country of the current user by the ISO code.
     * @return the country of the current user by the ISO code.
     * @throws SkypeException
     * @see #setCountryByISOCode(String)
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>
     */
    public String getCountryByISOCode() throws SkypeException {
        String value = getProperty("COUNTRY");
        return value.substring(0, value.indexOf(' '));
    }

    /**
     * Sets the country of the current user by the ISO code.
     * @param newValue the new country of the current user by the ISO code.
     * @throws SkypeException
     * @see #getCountryByISOCode()
     * @see <a href="http://en.wikipedia.org/wiki/ISO_3166-1">ISO 3166-1</a>
     */
    public void setCountryByISOCode(String newValue) throws SkypeException {
        if (newValue == null) {
            newValue = "";
        }
        setProperty("COUNTRY", newValue + " " + getCountry());
    }

    /**
     * Gets the country of the current user.
     * @return the country of the current user.
     * @throws SkypeException
     * @see #setCountry(String)
     */
    public String getCountry() throws SkypeException {
        String value = getProperty("COUNTRY");
        return value.substring(value.indexOf(' ') + 1);
    }

    /**
     * Sets the country of the current user.
     * @param newValue the new country of the current user.
     * @throws SkypeException
     * @see #getCountry()
     */
    public void setCountry(String newValue) throws SkypeException {
        if (newValue == null) {
            newValue = "";
        }
        setProperty("COUNTRY", getCountryByISOCode() + " " + newValue);
    }

    /**
     * Gets the province of the current user.
     * @return the province of the current user.
     * @throws SkypeException
     * @see #setProvince(String)
     */
    public String getProvince() throws SkypeException {
        return getProperty("PROVINCE");
    }

    /**
     * Sets the province of the current user.
     * @param newValue the new province of the current user.
     * @throws SkypeException
     * @see #getProvince()
     */
    public void setProvince(String newValue) throws SkypeException {
        setProperty("PROVINCE", newValue);
    }

    /**
     * Gets the city of the current user.
     * @return the city of the current user.
     * @throws SkypeException
     * @see #setCity(String)
     */
    public String getCity() throws SkypeException {
        return getProperty("CITY");
    }

    /**
     * Sets the city of the current user.
     * @param newValue the new city of the current user.
     * @throws SkypeException
     * @see #getCity()
     */
    public void setCity(String newValue) throws SkypeException {
        setProperty("CITY", newValue);
    }

    /**
     * Gets the home phone number of the current user.
     * @return the home phone number of the current user.
     * @throws SkypeException
     * @see #setHomePhoneNumber(String)
     */
    public String getHomePhoneNumber() throws SkypeException {
        return getProperty("PHONE_HOME");
    }

    /**
     * Sets the home phone number of the current user.
     * @param newValue the new home phone number of the current user.
     * @throws SkypeException
     * @see #getHomePhoneNumber()
     */
    public void setHomePhoneNumber(String newValue) throws SkypeException {
        setProperty("PHONE_HOME", newValue);
    }

    /**
     * Gets the office phone number of the current user.
     * @return the office phone number of the current user.
     * @throws SkypeException
     * @see #setOfficePhoneNumber(String)
     */
    public String getOfficePhoneNumber() throws SkypeException {
        return getProperty("PHONE_OFFICE");
    }

    /**
     * Sets the office phone number of the current user.
     * @param newValue the new office phone number of the current user.
     * @throws SkypeException
     * @see #getOfficePhoneNumber()
     */
    public void setOfficePhoneNumber(String newValue) throws SkypeException {
        setProperty("PHONE_OFFICE", newValue);
    }

    /**
     * Gets the mobile phone number of the current user.
     * @return the mobile phone number of the current user.
     * @throws SkypeException
     * @see #setMobilePhoneNumber(String)
     */
    public String getMobilePhoneNumber() throws SkypeException {
        return getProperty("PHONE_MOBILE");
    }

    /**
     * Sets the mobile phone number of the current user.
     * @param newValue the new mobile phone number of the current user.
     * @throws SkypeException
     * @see #getMobilePhoneNumber()
     */
    public void setMobilePhoneNumber(String newValue) throws SkypeException {
        setProperty("PHONE_MOBILE", newValue);
    }

    /**
     * Gets the home page address of the current user.
     * @return the home page address of the current user.
     * @throws SkypeException
     * @see #setHomePageAddress(String)
     */
    public String getHomePageAddress() throws SkypeException {
        return getProperty("HOMEPAGE");
    }

    /**
     * Sets the home page address of the current user.
     * @param newValue the new home page address of the current user.
     * @throws SkypeException
     * @see #getHomePageAddress()
     */
    public void setHomePageAddress(String newValue) throws SkypeException {
        setProperty("HOMEPAGE", newValue);
    }

    /**
     * Gets the introduction of the current user.
     * @return the introduction of the current user.
     * @throws SkypeException
     * @see #setIntroduction(String)
     */
    public String getIntroduction() throws SkypeException {
        return getProperty("ABOUT");
    }

    /**
     * Sets the introduction of the current user.
     * @param newValue the new introduction of the current user.
     * @throws SkypeException
     * @see #getIntroduction()
     */
    public void setIntroduction(String newValue) throws SkypeException {
        setProperty("ABOUT", newValue);
    }

    /**
     * Gets the mood message of the current user.
     * @return the mood message of the current user.
     * @throws SkypeException
     * @see #setMoodMessage(String)
     */
    public String getMoodMessage() throws SkypeException {
        return getProperty("MOOD_TEXT");
    }

    /**
     * Sets the mood message of the current user.
     * @param newValue the new mood message of the current user.
     * @throws SkypeException
     * @see #getMoodMessage()
     */
    public void setMoodMessage(String newValue) throws SkypeException {
        setProperty("MOOD_TEXT", newValue);
    }

    /**
     * Gets the time zone of the current user.
     * @return the time zone of the current user.
     * @throws SkypeException
     * @see #setTimeZone(int)
     */
    public int getTimeZone() throws SkypeException {
        return Integer.parseInt(getProperty("TIMEZONE"));
    }

    /**
     * Sets the time zone of the current user.
     * @param newValue the new time zone of the current user.
     * @throws SkypeException
     * @see #getTimeZone()
     */
    public void setTimeZone(int newValue) throws SkypeException {
        setProperty("TIMEZONE", "" + newValue);
    }

    /**
     * Indicates whether the current user has a web camera.
     * @return <code>true</code> if the current user has a web camera; <code>false</code> otherwise.
     * @throws SkypeException
     */
    public boolean isVideoCapable() throws SkypeException {
        return Boolean.parseBoolean(getProperty("IS_VIDEO_CAPABLE"));
    }

    /**
     * Gets the wait time in seconds before starting a call forwarding.
     * @return the wait time in seconds before starting a call forwarding.
     * @throws SkypeException
     * @see #setWaitTimeBeforeCallForwarding(int)
     */
    public int getWaitTimeBeforeCallForwarding() throws SkypeException {
        return Integer.parseInt(getProperty("CALL_NOANSWER_TIMEOUT"));
    }

    /**
     * Sets the wait time in seconds before starting a call forwarding.
     * @param newValue the new wait time in seconds before starting a call forwarding.
     * @throws SkypeException
     * @see #getWaitTimeBeforeCallForwarding()
     */
    public void setWaitTimeBeforeCallForwarding(int newValue) throws SkypeException {
        setProperty("CALL_NOANSWER_TIMEOUT", "" + newValue);
    }

    /**
     * Indicates whether the call forwarding function is on.
     * @return <code>true</code> if the call forwarding function is on; <code>false</code> otherwise.
     * @throws SkypeException
     * @see #setCallForwarding(boolean)
     */
    public boolean isCallForwarding() throws SkypeException {
        return Boolean.parseBoolean(getProperty("CALL_APPLY_CF"));
    }

    /**
     * Starts or stops the call forwarding function.
     * @param on if <code>true</code>, starts the call forwarding function; otherwise, stops the call forwarding function
     * @throws SkypeException
     * @see #isCallForwarding()
     */
    public void setCallForwarding(boolean on) throws SkypeException {
        setProperty("CALL_APPLY_CF", ("" + on).toUpperCase());
    }

    /**
     * Gets the all call forwarding rules of the current user.
     * @return the all call forwarding rules of the current user.
     * @throws SkypeException
     * @see #setAllCallForwardingRules(CallForwardingRule[])
     */
    public CallForwardingRule[] getAllCallForwardingRules() throws SkypeException {
        List<CallForwardingRule> rules = new ArrayList<CallForwardingRule>();
        for (String rule : getProperty("CALL_FORWARD_RULES").split(" ")) {
            String[] elements = rule.split(",");
            rules.add(new CallForwardingRule(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]), elements[2]));
        }
        return rules.toArray(new CallForwardingRule[0]);
    }

    /**
     * Sets the all call forwarding rules of the current user.
     * @param newValues the new all call forwarding rules of the current user.
     * @throws SkypeException
     * @see #getAllCallForwardingRules()
     */
    public void setAllCallForwardingRules(CallForwardingRule[] newValues) throws SkypeException {
        setProperty("CALL_FORWARD_RULES", toSpaceSeparatedString(newValues));
    }

    String[] getAllValidSMSNumbers() throws SkypeException {
        return Utils.convertToArray(getProperty("SMS_VALIDATED_NUMBERS"));
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("PROFILE", name);
    }

    private void setProperty(String name, String value) throws SkypeException {
        Utils.setProperty("PROFILE", name, value);
    }
}
