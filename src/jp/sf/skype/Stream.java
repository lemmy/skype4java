/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

import java.util.ArrayList;
import java.util.List;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;
import jp.sf.skype.connector.MessageProcessor;

public final class Stream {
    private final Application application;
    private final Friend friend;
    private final int number;
    private List<StreamListener> listeners = new ArrayList<StreamListener>();

    Stream(Application application, Friend friend, int number) {
        assert application != null;
        assert friend != null;
        this.application = application;
        this.friend = friend;
        this.number = number;
    }

    public Application getApplication() {
        return application;
    }

    public Friend getFriend() {
        return friend;
    }

    public int getNumber() {
        return number;
    }

    public String getId() {
        return getFriend().getId() + ":" + getNumber();
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
            listener.textReceived(text);
        }
    }

    void fireDatagramReceived(String datagram) {
        assert datagram != null;
        StreamListener[] listeners = this.listeners.toArray(new StreamListener[0]); // イベント通知中にリストが変更される可能性があるため
        for (StreamListener listener : listeners) {
            listener.datagramReceived(datagram);
        }
    }

    public void disconnect() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("ALTER APPLICATION " + application.getName() + " DISCONNECT " + friend.getId() + ":1");
            Utils.checkError(response);
            application.fireDisconnected(this);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
