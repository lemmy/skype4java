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

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorMessageEvent;

public final class Stream extends SkypeObject {
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
        return User.getFriendInstance(getId().substring(0, getId().indexOf(':')));
    }

    public void write(String text) throws SkypeException {
        Utils.checkNotNull(text, "text");
        try {
            final Object wait = new Object();
            AbstractConnectorListener connectorListener = new AbstractConnectorListener() {
                @Override
                public void messageReceived(ConnectorMessageEvent event) {
                    String message = event.getMessage();
                    if (message.startsWith("APPLICATION " + getApplication().getName() + " SENDING ")) {
                        String data = message.substring(("APPLICATION " + getApplication().getName() + " SENDING ").length());
                        if ("".equals(data)) {
                            synchronized(wait) {
                                wait.notify();
                            }
                            return;
                        }
                        String[] streams = data.split(" ");
                        for (String stream: streams) {
                            stream = stream.substring(0, stream.indexOf('='));
                            if (stream.equals(getId())) {
                                return;
                            }
                        }
                        synchronized(wait) {
                            wait.notify();
                        }
                    }
                }
            };
            ApplicationListener applicationListener = new ApplicationAdapter() {
                @Override
                public void disconnected(Stream stream) throws SkypeException {
                    if (stream == Stream.this) {
                        synchronized(wait) {
                            wait.notify();
                        }
                    }
                }
            };
            Connector.getInstance().addConnectorListener(connectorListener);
            application.addApplicationListener(applicationListener);
            String header = "ALTER APPLICATION " + getApplication().getName() + " WRITE " + getId();
            synchronized(wait) {
                String result = Connector.getInstance().executeWithId(header + " " + text, header);
                Utils.checkError(result);
                try {
                    // TODO Retuns when not attached to Skype
                    wait.wait();
                } catch(InterruptedException e) {
                    throw new SkypeException("The writing was interrupted.", e);
                } finally {
                    Connector.getInstance().removeConnectorListener(connectorListener);
                    application.removeApplicationListener(applicationListener);
                }
            }
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public void send(String datagram) throws SkypeException {
        Utils.checkNotNull(datagram, "datagram");
        try {
            String resposeHeader = "ALTER APPLICATION " + getApplication().getName() + " DATAGRAM " + getId();
            String command = resposeHeader + " " + datagram;
            String result = Connector.getInstance().execute(command, resposeHeader);
            Utils.checkError(result);
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
        StreamListener[] listeners = this.listeners.toArray(new StreamListener[0]);
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
        StreamListener[] listeners = this.listeners.toArray(new StreamListener[0]);
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
