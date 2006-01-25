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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Profile {
    public enum Status {
        UNKNOWN, ONLINE, OFFLINE, SKYPEME, AWAY, NA, DND, INVISIBLE, LOGGEDOUT;
    }

    public enum Sex {
        UNKNOWN, MALE, FEMALE;
    }

    public enum CallForwardingAction {
        REJECT, FORWARD, VOICEMAIL
    }

    public final class CallForwardingRule {
        private final int startSecond;
        private final int endSecond;
        private final String target;

        public CallForwardingRule(int startSecond, int endSecond, String target) {
            this.startSecond = startSecond;
            this.endSecond = endSecond;
            this.target = target;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object compared) {
            if (this == compared) {
                return true;
            }
            if (compared instanceof CallForwardingRule) {
                return toString().equals(((Profile) compared).toString());
            }
            return false;
        }

        @Override
        public String toString() {
            return startSecond + "," + endSecond + "," + target;
        }

        public int getEndSecond() {
            return endSecond;
        }

        public int getStartSecond() {
            return startSecond;
        }

        public String getTarget() {
            return target;
        }
    }

    Profile() {
    }

    public String getId() throws SkypeException {
        return Utils.getProperty("CURRENTUSERHANDLE");
    }

    public Status getStatus() throws SkypeException {
        return Status.valueOf(Utils.getProperty("USERSTATUS"));
    }

    public void setStatus(Status status) throws SkypeException {
        Utils.setProperty("USERSTATUS", status.toString());
    }

    public boolean canDoSkypeOut() throws SkypeException {
        return canDo("SKYPEOUT");
    }

    public boolean canDoSkypeIn() throws SkypeException {
        return canDo("SKYPEIN");
    }

    public boolean canDoVoiceMail() throws SkypeException {
        return canDo("VOICEMAIL");
    }

    private boolean canDo(String name) throws SkypeException {
        return Boolean.parseBoolean(Utils.getProperty("PRIVILEGE", name));
    }

    public int getPSTNBalance() throws SkypeException {
        return Integer.parseInt(getProfileProperty("PSTN_BALANCE"));
    }

    public String getPSTNBalanceCurrencyUnit() throws SkypeException {
        return getProfileProperty("PSTN_BALANCE_CURRENCY");
    }

    public String getFullName() throws SkypeException {
        return getProfileProperty("FULLNAME");
    }

    public void setFullName(String newValue) throws SkypeException {
        setProfileProperty("FULLNAME", newValue);
    }

    public Date getBirthDay() throws SkypeException {
        String value = getProfileProperty("BIRTHDAY");
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

    public void setBirthDay(Date newValue) throws SkypeException {
        String newValueString;
        if (newValue == null) {
            newValueString = "0";
        } else {
            newValueString = new SimpleDateFormat("yyyyMMdd").format(newValue);
        }
        setProfileProperty("BIRTHDAY", newValueString);
    }

    public Sex getSex() throws SkypeException {
        return Sex.valueOf((getProfileProperty("SEX")));
    }

    public void setSex(Sex newValue) throws SkypeException {
        setProfileProperty("SEX", newValue.toString());
    }

    public String[] getAllLauguages() throws SkypeException {
        return getProfileProperty("LANGUAGES").split(" ");
    }

    public void setAllLanguages(String[] newValues) throws SkypeException {
        if (newValues == null) {
            newValues = new String[0];
        }
        setProfileProperty("LANGUAGES", toSpaceSeparatedString(newValues));
    }

    private String toSpaceSeparatedString(Object[] newValues) {
        StringBuilder newValuesString = new StringBuilder();
        for (Object newValue : newValues) {
            newValuesString.append(newValue);
        }
        return newValuesString.toString();
    }

    // http://ja.wikipedia.org/wiki/%E5%9B%BD%E5%90%8D%E3%82%B3%E3%83%BC%E3%83%89
    public void setCountryByISOCode(String newValue) throws SkypeException {
        if (newValue == null) {
            newValue = "";
        }
        setProfileProperty("COUNTRY", newValue + " " + getCountry());
    }

    public void setCountry(String newValue) throws SkypeException {
        if (newValue == null) {
            newValue = "";
        }
        setProfileProperty("COUNTRY", getCountryByISOCode() + " " + newValue);
    }

    public String getCountryByISOCode() throws SkypeException {
        String value = getProfileProperty("COUNTRY");
        return value.substring(0, value.indexOf(' '));
    }

    public String getCountry() throws SkypeException {
        String value = getProfileProperty("COUNTRY");
        return value.substring(value.indexOf(' ') + 1);
    }

    public String getProvince() throws SkypeException {
        return getProfileProperty("PROVINCE");
    }

    public void setProvince(String newValue) throws SkypeException {
        setProfileProperty("PROVINCE", newValue);
    }

    public String getCity() throws SkypeException {
        return getProfileProperty("CITY");
    }

    public void setCity(String newValue) throws SkypeException {
        setProfileProperty("CITY", newValue);
    }

    public String getHomePhoneNumber() throws SkypeException {
        return getProfileProperty("PHONE_HOME");
    }

    public void setHomePhoneNumber(String newValue) throws SkypeException {
        setProfileProperty("PHONE_HOME", newValue);
    }

    public String getOfficePhoneNumber() throws SkypeException {
        return getProfileProperty("PHONE_OFFICE");
    }

    public void setOfficePhoneNumber(String newValue) throws SkypeException {
        setProfileProperty("PHONE_OFFICE", newValue);
    }

    public String getMobilePhoneNumber() throws SkypeException {
        return getProfileProperty("PHONE_MOBILE");
    }

    public void setMobilePhoneNumber(String newValue) throws SkypeException {
        setProfileProperty("PHONE_MOBILE", newValue);
    }

    public String getHomePageAddress() throws SkypeException {
        return getProfileProperty("HOMEPAGE");
    }

    public void setHomePageAddress(String newValue) throws SkypeException {
        setProfileProperty("HOMEPAGE", newValue);
    }

    public String getIntroduction() throws SkypeException {
        return getProfileProperty("ABOUT");
    }

    public void setIntroduction(String newValue) throws SkypeException {
        setProfileProperty("ABOUT", newValue);
    }

    public String getMoodMessage() throws SkypeException {
        return getProfileProperty("MOOD_TEXT");
    }

    public void setMoodMessage(String newValue) throws SkypeException {
        setProfileProperty("MOOD_TEXT", newValue);
    }

    public int getTimeZone() throws SkypeException {
        return Integer.parseInt(getProfileProperty("TIMEZONE"));
    }

    public void setTimeZone(int newValue) throws SkypeException {
        setProfileProperty("TIMEZONE", "" + newValue);
    }

    public boolean isVideoCapable() throws SkypeException {
        return Boolean.parseBoolean(getProfileProperty("IS_VIDEO_CAPABLE"));
    }

    public int getWaitTimeBeforeCallForwarding() throws SkypeException {
        return Integer.parseInt(getProfileProperty("CALL_NOANSWER_TIMEOUT"));
    }

    public void setWaitTimeBeforeCallForwarding(int second) throws SkypeException {
        setProfileProperty("CALL_NOANSWER_TIMEOUT", "" + second);
    }

    public CallForwardingAction getCallForwardingAction() throws SkypeException {
        return CallForwardingAction.valueOf(getProfileProperty("CALL_NOANSWER_ACTION"));
    }

    public void setCallForwardingAction(CallForwardingAction newValue) throws SkypeException {
        setProfileProperty("CALL_NOANSWER_ACTION", newValue.toString());
    }

    public CallForwardingRule[] getAllCallForwardingRules() throws SkypeException {
        List<CallForwardingRule> rules = new ArrayList<CallForwardingRule>();
        for (String rule : getProfileProperty("CALL_FORWARD_RULES").split(" ")) {
            String[] elements = rule.split(",");
            rules.add(new CallForwardingRule(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]), elements[2]));
        }
        return rules.toArray(new CallForwardingRule[0]);
    }

    public void setAllCallForwardingRules(CallForwardingRule[] newValues) throws SkypeException {
        setProfileProperty("CALL_FORWARD_RULES", toSpaceSeparatedString(newValues));
    }

    private String getProfileProperty(String name) throws SkypeException {
        return Utils.getProperty("PROFILE", name);
    }

    private void setProfileProperty(String name, String value) throws SkypeException {
        Utils.setProperty("PROFILE", name, value);
    }
}
