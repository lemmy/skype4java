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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

public final class EventMessage extends SkypeObject {
    private static final Map<String, EventMessage> eventMessages = Collections.synchronizedMap(new HashMap<String, EventMessage>());
    private static final AtomicInteger eventMessageNumber = new AtomicInteger();

    private static Object eventMessageListenerMutex = new Object();
    private static ConnectorListener eventMessageListener;

    static EventMessage getInstance(final String id) {
        return eventMessages.get(id);
    }

    static EventMessage addEventMessage(String caption, String hint) {
        EventMessage eventMessage = new EventMessage(caption, hint);
        eventMessages.put(eventMessage.getId(), eventMessage);
        return eventMessage;
    }
    
    private final String id = "eventMessage" + eventMessageNumber.getAndIncrement();
    private final String caption;
    private final String hint;

    private final List<EventMessageListener> eventMessageListeners = Collections.synchronizedList(new ArrayList<EventMessageListener>());

    EventMessage(final String caption, final String hint) {
        this.caption = caption;
        this.hint = hint;
    }
    
    public int hashCode() {
        return getId().hashCode();
    }

    public boolean equals(final Object compared) {
        if (compared instanceof EventMessage) {
            return getId().equals(((EventMessage)compared).getId());
        }
        return false;
    }

    String getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }

    public String getHint() {
        return hint;
    }

    public void addEventMessageListener(EventMessageListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        eventMessageListeners.add(listener);
        synchronized (eventMessageListenerMutex) {
            if (eventMessageListener == null) {
                eventMessageListener = new AbstractConnectorListener() {
                    public void messageReceived(ConnectorMessageEvent event) {
                        String message = event.getMessage();
                        if (message.startsWith("EVENT ") && message.endsWith(" CLICKED")) {
                            String id = message.substring("EVENT ".length(), message.length() - " CLICKED".length());
                            EventMessage eventMessage = EventMessage.getInstance(id);
                            if (eventMessage != null) {
                                EventMessageListener[] listeners = eventMessage.eventMessageListeners.toArray(new EventMessageListener[0]);
                                for (EventMessageListener listener : listeners) {
                                    try {
                                        listener.eventMessageClicked();
                                    } catch (Throwable e) {
                                        Skype.handleUncaughtException(e);
                                    }
                                }
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorListener(eventMessageListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    public void removeEventMessageListener(EventMessageListener listener) {
        Utils.checkNotNull("listener", listener);
        eventMessageListeners.remove(listener);
        synchronized (eventMessageListenerMutex) {
            boolean isEmpty = true;
            for (EventMessage eventMessage: eventMessages.values()) {
                isEmpty &= eventMessage.eventMessageListeners.isEmpty();
            }
            if (isEmpty) {
                Connector.getInstance().removeConnectorListener(eventMessageListener);
                eventMessageListener = null;
            }
        }
    }

    public void dispose() throws SkypeException {
        try {
            String command = "DELETE EVENT " + getId();
            String response = Connector.getInstance().execute(command);
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }
}
