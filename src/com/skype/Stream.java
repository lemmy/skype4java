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

import java.util.ArrayList;
import java.util.List;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.MessageProcessor;

public final class Stream {
    private final Application application;
    private final String id;

    private List<StreamListener> listeners = new ArrayList<StreamListener>();
    private SkypeExceptionHandler exceptionHandler;
    
    Stream(Application application, String id) {
        assert application != null;
        assert id != null;
        this.application = application;
        this.id = id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object compared) {
        if (compared instanceof Stream) {
            Stream comparedStream = (Stream)compared;
            return getId().equals(comparedStream.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return getId();
    }

    public Application getApplication() {
        return application;
    }

    public String getId() {
        return id;
    }

    public Friend getFriend() {
        return new Friend(getId().substring(0, getId().indexOf(':')));
    }

    public void write(String text) throws SkypeException {
        Utils.checkNotNull(text, "text");
        try {
            final String[] error = new String[1];
            MessageProcessor processor = new MessageProcessor() {
                public void messageReceived(String message) {
                    if (message.startsWith("APPLICATION " + getApplication().getName() + " SENDING " + getId())) {
                    } else if (message.equals("APPLICATION " + getApplication().getName() + " SENDING ")) {
                        releaseLock();
                    } else if (message.startsWith("ERROR ")) {
                        error[0] = message.substring("ERROR ".length());
                        releaseLock();
                    }
                }
            };
            Connector.getInstance().execute("ALTER APPLICATION " + getApplication().getName() + " WRITE " + getId() + " " + text, processor);
            Utils.checkError(error[0]);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public void send(String datagram) throws SkypeException {
        Utils.checkNotNull(datagram, "datagram");
        try {
            final String[] error = new String[1];
            MessageProcessor processor = new MessageProcessor() {
                public void messageReceived(String message) {
                    if (message.startsWith("APPLICATION " + getApplication().getName() + " SENDING " + getId())) {
                    } else if (message.equals("APPLICATION " + getApplication().getName() + " SENDING ")) {
                        releaseLock();
                    } else if (message.startsWith("ERROR ")) {
                        error[0] = message.substring("ERROR ".length());
                        releaseLock();
                    }
                }
            };
            Connector.getInstance().execute("ALTER APPLICATION " + getApplication().getName() + " DATAGRAM " + getId() + " " + datagram, processor);
            Utils.checkError(error[0]);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public void addStreamListener(StreamListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.add(listener);
    }

    public void removeStreamListener(StreamListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(listener);
    }

    void fireTextReceived(String text) {
        assert text != null;
        StreamListener[] listeners = this.listeners.toArray(new StreamListener[0]); // イベント通知中にリストが変更される可能性があるため
        for (StreamListener listener : listeners) {
            try {
                listener.textReceived(text);
            } catch (Throwable e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    void fireDatagramReceived(String datagram) {
        assert datagram != null;
        StreamListener[] listeners = this.listeners.toArray(new StreamListener[0]); // イベント通知中にリストが変更される可能性があるため
        for (StreamListener listener : listeners) {
            try {
                listener.datagramReceived(datagram);
            } catch (Throwable e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    public void disconnect() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("ALTER APPLICATION " + application.getName() + " DISCONNECT " + getId());
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
