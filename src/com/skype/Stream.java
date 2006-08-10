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

/**
 * This class implements a simple way of sending and receiving AP2AP data.
 */
public final class Stream extends SkypeObject {

	/** AP2AP application to which this Stream is connected. */
	private final Application application;
	/** ID of this stream. */
    private final String id;

    /** Listeners to this stream. */
    private List<StreamListener> listeners = new ArrayList<StreamListener>();
    /** Exceptionhandler for this Stream. */
    private SkypeExceptionHandler exceptionHandler;
    
    /**
     * Constructor.
     * @param newApplication AP2AP application to which this stream belongs.
     * @param newId ID of this stream.
     */
    Stream(Application newApplication, String newId) {
        assert newApplication != null;
        assert newId != null;
        this.application = newApplication;
        this.id = newId;
    }

    /**
     * Overridden to use ID as hashcode.
     * @return ID.
     */
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Overridden to compare Stream objects based on ID.
     * @param compared the object to compare to.
     * @return true if ID's are equal.
     */
    public boolean equals(Object compared) {
        if (compared instanceof Stream) {
            Stream comparedStream = (Stream)compared;
            return getId().equals(comparedStream.getId());
        }
        return false;
    }

    /**
     * Return ID as String representation.
     * @return ID.
     */
    public String toString() {
        return getId();
    }

    /**
     * Return the application this stream belongs to.
     * @return the AP2AP application.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Return ID of this Stream.
     * @return ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the User which is on the other end of this Stream.
     * @return User.
     */
    public Friend getFriend() {
        return User.getFriendInstance(getId().substring(0, getId().indexOf(':')));
    }

    /**
     * Send a message through this Stream.
     * @param text The message to send.
     * @throws SkypeException when the connection to the Skype client has gone bad or AP2AP connection is not ok.
     */
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

    /**
     * Send a datagram message through this stream.
     * @param datagram the data message to send.
     * @throws SkypeException when the Skype client connection has gone bad or when AP2AP connection is not ok.
     */
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

    /**
     * Add a listener to this AP2AP Stream.
     * @param listener listener to add to the collection of listeners.
     */
    public void addStreamListener(StreamListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.add(listener);
    }

    /**
     * Remove a listener from the collection of listeners to this Stream.
     * @param listener the listener to remove.
     */
    public void removeStreamListener(StreamListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(listener);
    }

    /**
     * Fire all listeners when a text message is received through this AP2AP Stream.
     * @param text the message that is received.
     */
    void fireTextReceived(String text) {
        assert text != null;
        StreamListener[] tmpListeners = this.listeners.toArray(new StreamListener[0]);
        for (StreamListener listener : tmpListeners) {
            try {
                listener.textReceived(text);
            } catch (Throwable e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    /**
     * Fire all listeners when a datagram message is received through this AP2AP Stream.
     * @param datagram The datagram message that has been received.
     */
    void fireDatagramReceived(String datagram) {
        assert datagram != null;
        StreamListener[] tmpListeners = this.listeners.toArray(new StreamListener[0]);
        for (StreamListener listener : tmpListeners) {
            try {
                listener.datagramReceived(datagram);
            } catch (Throwable e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    /**
     * Disconenct this Stream and close the AP2AP connection.
     * @throws SkypeException when the  Skype client connection has gone bad.
     */
    public void disconnect() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("ALTER APPLICATION " + application.getName() + " DISCONNECT " + getId());
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
