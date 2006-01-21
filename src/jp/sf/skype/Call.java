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

import java.util.Date;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;

public final class Call {
    /*
     * 発信の状態 ROUTING (経路探索) > RINGING (呼び出し中) > INPROGRESS (会話開始) > FINISHED
     * (会話終了) > MISSED (発信側がキャンセル) > REFUSED (受信側がキャンセル)
     */
    public enum Status {
        UNPLACED, ROUTING, EARLYMEDIA, FAILED, RINGING, INPROGRESS, ONHOLD, FINISHED, MISSED, REFUSED, BUSY, CANCELLED, VM_BUFFERING_GREETING, VM_PLAYING_GREETING, VM_RECORDING, VM_UPLOADING, VM_SENT, VM_CANCELLED, VM_FAILED
    }

    public enum Type {
        INCOMING_PSTN, OUTGOING_PSTN, INCOMING_P2P, OUTGOING_P2P;
    }

    public enum VideoStatus {
        NOT_AVAILABLE, AVAILABLE, STARTING, REJECTED, RUNNING, STOPPING, PAUSED;
    }

    private enum VideoEnabled {
        VIDEO_NONE, VIDEO_SEND_ENABLED, VIDEO_RECV_ENABLED, VIDEO_BOTH_ENABLED;
    }

    private final String id;

    Call(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void hold() throws SkypeException {
        setStatus("ONHOLD");
    }

    public void resume() throws SkypeException {
        setStatus("INPROGRESS");
    }

    public void finish() throws SkypeException {
        setStatus("FINISHED");
    }

    public void answer() throws SkypeException {
        setStatus("INPROGRESS");
    }

    public void cancel() throws SkypeException {
        setStatus("FINISHED");
    }

    private void setStatus(String status) throws SkypeException {
        try {
            String response = Connector.getInstance().execute("SET CALL " + getId() + " STATUS " + status, "CALL " + getId() + " STATUS ");
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public Date getStartTime() throws SkypeException {
        return Utils.parseUnixTime(getProperty("TIMESTAMP"));
    }

    public Friend getParter() throws SkypeException {
        return Skype.getContactList().getFriend(getPartnerId());
    }

    public String getPartnerId() throws SkypeException {
        return getProperty("PARTNER_HANDLE");
    }

    public String getPartnerDisplayName() throws SkypeException {
        return getProperty("PARTNER_DISPNAME");
    }

    public Type getType() throws SkypeException {
        return Type.valueOf(getProperty("TYPE"));
    }

    public Status getStatus() throws SkypeException {
        return Status.valueOf(getProperty("STATUS"));
    }

    public int getDuration() throws SkypeException {
        return Integer.parseInt(getProperty("DURATION"));
    }

    public int getErrorCode() throws SkypeException {
        return Integer.parseInt(getProperty("FAILUREREASON"));
    }

    public void setReceiveVideoEnabled(boolean on) throws SkypeException {
        String value = on ? "START_VIDEO_SEND" : "STOP_VIDEO_SEND";
        try {
            String response = Connector.getInstance().execute("ALTER CALL " + getId() + " " + value);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public boolean isReceiveVideoEnabled() throws SkypeException {
        VideoEnabled enabled = VideoEnabled.valueOf(getProperty("VIDEO_STATUS"));
        switch (enabled) {
            case VIDEO_NONE:
            case VIDEO_SEND_ENABLED:
                return false;
            case VIDEO_RECV_ENABLED:
            case VIDEO_BOTH_ENABLED:
                return true;
            default:
                return false;
        }
    }

    public void setSendVideoEnabled(boolean on) throws SkypeException {
        String value = on ? "START_VIDEO_RECEIVE" : "STOP_VIDEO_RECEIVE";
        try {
            String response = Connector.getInstance().execute("ALTER CALL " + getId() + " " + value);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public boolean isSendVideoEnabled() throws SkypeException {
        VideoEnabled enabled = VideoEnabled.valueOf(getProperty("VIDEO_STATUS"));
        switch (enabled) {
            case VIDEO_NONE:
            case VIDEO_RECV_ENABLED:
                return false;
            case VIDEO_SEND_ENABLED:
            case VIDEO_BOTH_ENABLED:
                return true;
            default:
                return false;
        }
    }

    public VideoStatus getReceiveVideoStatus() throws SkypeException {
        return VideoStatus.valueOf(getProperty("VIDEO_RECEIVE_STATUS"));
    }

    public VideoStatus getSendVideoStatus() throws SkypeException {
        return VideoStatus.valueOf(getProperty("VIDEO_SEND_STATUS"));
    }

    private String getProperty(String name) throws SkypeException {
        return Utils.getProperty("CALL", getId(), name);
    }
}
