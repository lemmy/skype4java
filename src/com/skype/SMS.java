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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public final class SMS {
    public enum Type {
        INCOMING, OUTGOING, CONFIRMATION_CODE_REQUEST, CONFIRMATION_CODE_SUBMIT, UNKNOWN;
    }

    public enum Status {
        RECEIVED, READ, COMPOSING, SENDING_TO_SERVER, SENT_TO_SERVER, DELIVERED, SOME_TARGETS_FAILED, FAILED, UNKNOWN;
    }

    public enum FailureReason {
        MISC_ERROR, SERVER_CONNECT_FAILED, NO_SMS_CAPABILITY, INSUFFICIENT_FUNDS, INVALID_CONFIRMATION_CODE, USER_BLOCKED, IP_BLOCKED, NODE_BLOCKED, UNKNOWN;
    }

    public static final class TargetStatus {
        public enum Status {
            TARGET_ANALYZING, TARGET_UNDEFINED, TARGET_ACCEPTABLE, TARGET_NOT_ROUTABLE, TARGET_DELIVERY_PENDING, TARGET_DELIVERY_SUCCESSFUL, TARGET_DELIVERY_FAILED, UNKNOWN;
        }
        
        private final String number;
        private final Status status;
        
        TargetStatus(String number, Status status) {
            assert number != null;
            assert status != null;
            this.number = number;
            this.status = status;
        }

        @Override
        public int hashCode() {
            return (number + "/" + status).hashCode();
        }

        @Override
        public boolean equals(Object compared) {
            if (compared instanceof TargetStatus) {
                TargetStatus comparedTargetStatus = (TargetStatus)compared;
                return comparedTargetStatus.number.equals(number) && comparedTargetStatus.status.equals(status);
            }
            return false;
        }

        public String getNumber() {
            return number;
        }

        public Status getStatus() {
            return status;
        }
    }

    private final String id;

    SMS(String id) {
        assert id != null;
        this.id = id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (compared instanceof Chat) {
            return getId().equals(((Chat) compared).getId());
        }
        return false;
    }

    public String getId() {
        return id;
    }

    public String getContent() throws SkypeException {
        return getProperty("BODY");
    }

    void setContent(String newValue) throws SkypeException {
        setSMSProperty("BODY", newValue);
    }

    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    public Status getStatus() throws SkypeException {
        return Status.valueOf(getProperty("STATUS"));
    }

    public FailureReason getFailureReason() throws SkypeException {
        return FailureReason.valueOf(getProperty("FAILUREREASON"));
    }

    public boolean isCheckedFailure() throws SkypeException {
        return !Boolean.parseBoolean(getProperty("IS_FAILED_UNSEEN"));
    }

    public void toCheckedFailure() throws SkypeException {
        try {
            String command = "SET SMS " + getId() + " SEEN";
            String response = Connector.getInstance().execute(command);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public Date getTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    public BigDecimal getPrice() throws SkypeException {
        return new BigDecimal(getProperty("PRICE")).scaleByPowerOfTen(-Integer.parseInt(getProperty("PRICE_PRECISION")));
    }

    public String getCurrencyUnit() throws SkypeException {
        return getProperty("PRICE_CURRENCY");
    }

    public String getReplyToNumber() throws SkypeException {
        return getProperty("REPLY_TO_NUMBER");
    }

    public void setReplyToNumber(String newValue) throws SkypeException {
        setSMSProperty("REPLY_TO_NUMBER", newValue);
    }

    public String[] getAllTargetNumbers() throws SkypeException {
        return getProperty("TARGET_NUMBERS").split(", ");
    }

    public void setAllTargetNumbers(String[] newValues) throws SkypeException {
        setSMSProperty("TARGET_NUMBERS", Utils.convertToCommaSeparatedString(newValues));
    }

    public TargetStatus[] getAllTargetStatuses() throws SkypeException {
        String data = getProperty("TARGET_STATUSES");
        List<TargetStatus> r = new ArrayList<TargetStatus>();
        for (String targetStatus: data.split(", ")) {
            String[] elements = targetStatus.split("=");
            r.add(new TargetStatus(elements[0], TargetStatus.Status.valueOf(elements[1])));
        }
        return r.toArray(new TargetStatus[0]);
    }

    public String[] getAllContentChunks() throws SkypeException {
        int chunkCount = Integer.parseInt(getProperty("CHUNKING"));
        String[] r = new String[chunkCount];
        for (int i = 0; i < chunkCount; i++) {
            r[i] = getProperty("CHUNK " + i);
        }
        return r;
    }

    void send() throws SkypeException {
        Utils.executeWithErrorCheck("ALTER SMS " + getId() + " SEND");
    }

    public void delete() throws SkypeException {
        Utils.executeWithErrorCheck("DELETE SMS " + getId());
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("SMS", getId(), name);
    }

    private void setSMSProperty(String name, String value) throws SkypeException {
        Utils.setProperty("SMS", getId(), name, value);
    }
}
