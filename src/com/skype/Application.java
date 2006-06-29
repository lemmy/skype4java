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

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorMessageReceivedListener;

public final class Application {
    private final String name;

    private final Object connectMutex = new Object();

    private final List<ApplicationListener> listeners = Collections.synchronizedList(new ArrayList<ApplicationListener>());
    private final Map<String, Stream> streams = new HashMap<String, Stream>();
    private final ConnectorMessageReceivedListener dataListener = new DataListener();

    private boolean isFinished;
    private final Object isFinishedFieldMutex = new Object();
    private Thread shutdownHookForFinish;
    
    private SkypeExceptionHandler exceptionHandler;

    Application(String name) {
        assert name != null;
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    };

    void initialize() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("CREATE APPLICATION " + name);
            Utils.checkError(response);
            Connector.getInstance().addConnectorMessageReceivedListener(dataListener);
            shutdownHookForFinish = new Thread() {
                @Override
                public void run() {
                    try {
                        Connector.getInstance().execute("DELETE APPLICATION " + Application.this.getName());
                    } catch (ConnectorException e) {
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHookForFinish);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public void finish() throws SkypeException {
        synchronized (isFinishedFieldMutex) {
            if (!isFinished) {
                Connector.getInstance().removeConnectorMessageReceivedListener(dataListener);
                Runtime.getRuntime().removeShutdownHook(shutdownHookForFinish);
                isFinished = true;
                try {
                    String response = Connector.getInstance().execute("DELETE APPLICATION " + getName());
                    Utils.checkError(response);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public Stream[] connectToAll() throws SkypeException {
        return connect(getAllConnectableFriends());
    }

    public Stream[] connect(Friend... friends) throws SkypeException {
        Utils.checkNotNull("friends", friends);
        synchronized(connectMutex) {
            try {
                final Object wait = new Object();
                ConnectorMessageReceivedListener connectorListener = new ConnectorMessageReceivedListener() {
                    public void messageReceived(String receivedMessage) {
                        if (receivedMessage.equals("APPLICATION " + getName() + " CONNECTING ")) {
                            synchronized(wait) {
                                wait.notify();
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorMessageReceivedListener(connectorListener);
                    synchronized(wait) {
                        for (Friend friend: friends) {
                            String result = Connector.getInstance().execute("ALTER APPLICATION " + getName() + " CONNECT " + friend.getId());
                            Utils.checkError(result);
                        }
                        try {
                            wait.wait();
                        } catch(InterruptedException e) {
                            throw new SkypeException("The connecting was interrupted.", e);
                        }
                    }
                    return getAllStreams(friends);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                    return null;
                } finally {
                    Connector.getInstance().removeConnectorMessageReceivedListener(connectorListener);
                }
            } catch (SkypeException e) {
                for (Stream stream: getAllStreams(friends)) {
                    try {
                        stream.disconnect();
                    } catch(SkypeException e2) {
                        // do nothing
                    }
                }
                throw e;
            }
        }
    }

    public Stream[] getAllStreams(Friend... friends) throws SkypeException {
        List<Stream> results = new ArrayList<Stream>();
        for (Stream stream: getAllStreams()) {
            Friend friend = stream.getFriend();
            for (Friend comparedFriend: friends) {
                if (friend.equals(comparedFriend)) {
                    results.add(stream);
                }
            }
        }
        return results.toArray(new Stream[0]);
    }

    public Stream[] getAllStreams() throws SkypeException {
        String streamIds = Utils.getPropertyWithCommandId("APPLICATION", getName(), "STREAMS");
        synchronized(streams) {
            fireStreamEvents(streamIds);
            if ("".equals(streamIds)) {
                return new Stream[0];
            }
            String[] ids = streamIds.split(" ");
            Stream[] results = new Stream[ids.length];
            for (int i = 0; i < ids.length; i++) {
                results[i] = streams.get(ids[i]);
            }
            return results;
        }
    }

    private void fireStreamEvents(String newStreamIdList) {
        synchronized(streams) {
            String[] newStreamIds = "".equals(newStreamIdList) ? new String[0]: newStreamIdList.split(" ");
            for (String streamId: newStreamIds) {
                if (!streams.containsKey(streamId)) {
                    Stream stream = new Stream(this, streamId);
                    streams.put(streamId, stream);
                    fireConnected(stream);
                }
            }
            String[] oldStreamIds = streams.keySet().toArray(new String[0]);
            NEXT: for (String oldStreamId: oldStreamIds) {
                for (String newStreamId: newStreamIds) {
                    if (oldStreamId.equals(newStreamId)) {
                        continue NEXT;
                    }
                }
                Stream stream = streams.remove(oldStreamId);
                fireDisconnected(stream);
            }
        }
    }

    private void fireConnected(Stream stream) {
        assert stream != null;
        ApplicationListener[] listeners = this.listeners.toArray(new ApplicationListener[0]); // to prevent ConcurrentModificationException
        for (ApplicationListener listener : listeners) {
            try {
                listener.connected(stream);
            } catch(SkypeException e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    private void fireDisconnected(Stream stream) {
        assert stream != null;
        ApplicationListener[] listeners = this.listeners.toArray(new ApplicationListener[0]); // to prevent ConcurrentModificationException
        for (ApplicationListener listener : listeners) {
            try {
                listener.disconnected(stream);
            } catch (SkypeException e) {
                Utils.handleUncaughtException(e, exceptionHandler);
            }
        }
    }

    public void addApplicationListener(ApplicationListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.add(listener);
    }

    public void removeApplicationListener(ApplicationListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(listener);
    }

    private class DataListener implements ConnectorMessageReceivedListener {
        public void messageReceived(final String message) {
            String streamsHeader = "APPLICATION " + getName() + " STREAMS ";
            if (message.startsWith(streamsHeader)) {
                String streamIds = message.substring(streamsHeader.length());
                fireStreamEvents(streamIds);
            }
            final String dataHeader = "APPLICATION " + getName() + " ";
            if (message.startsWith(dataHeader)) {
                handleData(message.substring(dataHeader.length()));
            }
        }

        private void handleData(String dataResponse) {
            try {
                if (isReceivedText(dataResponse)) {
                    String data = dataResponse.substring("RECEIVED ".length());
                    String streamId = data.substring(0, data.indexOf('='));
                    String dataHeader = "ALTER APPLICATION " + getName() + " READ " + streamId;
                    String response = Connector.getInstance().executeWithId(dataHeader, dataHeader);
                    Utils.checkError(response);
                    String text = response.substring(dataHeader.length() + 1);
                    synchronized(streams) {
                        if (streams.containsKey(streamId)) {
                            streams.get(streamId).fireTextReceived(text);
                        }
                    }
                } else if (isReceivedDatagram(dataResponse)) {
                    String data = dataResponse.substring("DATAGRAM ".length());
                    String streamId = data.substring(0, data.indexOf(' '));
                    String datagram = data.substring(data.indexOf(' ') + 1);
                    synchronized(streams) {
                        if (streams.containsKey(streamId)) {
                            streams.get(streamId).fireDatagramReceived(datagram);
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("can't handle data", e);
            }
        }

        private boolean isReceivedText(String dataResponse) {
            return dataResponse.startsWith("RECEIVED ") && ("RECEIVED ".length() < dataResponse.length());
        }

        private boolean isReceivedDatagram(String dataResponse) {
            return dataResponse.startsWith("DATAGRAM ");
        }
    }

    public Friend[] getAllConnectableFriends() throws SkypeException {
        return getAllFriends("CONNECTABLE");
    }

    public Friend[] getAllConnectingFriends() throws SkypeException {
        return getAllFriends("CONNECTING");
    }

    public Friend[] getAllConnectedFriends() throws SkypeException {
        return getAllFriends("STREAMS");
    }

    public Friend[] getAllSendingFriends() throws SkypeException {
        return getAllFriends("SENDING");
    }

    public Friend[] getAllReceivedFriends() throws SkypeException {
        return getAllFriends("RECEIVED");
    }

    private Friend[] getAllFriends(String type) throws SkypeException {
        try {
            String responseHeader = "APPLICATION " + getName() + " " + type + " ";
            String response = Connector.getInstance().executeWithId("GET APPLICATION " + getName() + " " + type, responseHeader);
            Utils.checkError(response);
            return extractFriends(response.substring(responseHeader.length()));
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    private Friend[] extractFriends(String list) throws SkypeException {
        assert list != null;
        if ("".equals(list)) {
            return new Friend[0];
        }
        String[] ids = list.split(" ");
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            if (id.contains(":")) {
                ids[i] = id.substring(0, id.indexOf(':'));
            }
        }
        List<Friend> friends = new ArrayList<Friend>();
        for (String id : ids) {
            Friend friend = Skype.getContactList().getFriend(id);
            if (!friends.contains(friend)) {
                friends.add(friend);
            }
        }
        return friends.toArray(new Friend[0]);
    }
}
