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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;
import jp.sf.skype.connector.ConnectorListener;
import jp.sf.skype.connector.MessageProcessor;

public final class Application {
    private final String name;
    private List<ApplicationListener> listeners = new ArrayList<ApplicationListener>();
    private Map<String, Stream> streams = new HashMap<String, Stream>();
    private ConnectorListener dataListener = new DataListener();

    Application(String name) {
        assert name != null;
        this.name = name;
    }

    void initalize() throws SkypeException {
        try {
            String createResponse = Connector.getInstance().execute("CREATE APPLICATION " + name);
            try {
                Utils.checkError(createResponse);
            } catch (SkypeException e) {
                String deleteResponse = Connector.getInstance().execute("DELETE APPLICATION " + getName());
                Utils.checkError(deleteResponse);
                String retryResponse = Connector.getInstance().execute("CREATE APPLICATION " + name);
                Utils.checkError(retryResponse);
            }
            Connector.getInstance().addConnectorListener(dataListener);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public void finish() throws SkypeException {
        try {
            String response = Connector.getInstance().execute("DELETE APPLICATION " + getName());
            Utils.checkError(response);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
        Connector.getInstance().removeConnectorListener(dataListener);
    }

    private void handleData(String dataResponse) {
        try {
            if (isReceivedText(dataResponse)) {
                String data = dataResponse.substring("RECEIVED ".length());
                String streamId = data.substring(0, data.indexOf('='));
                String dataHeader = "ALTER APPLICATION " + getName() + " READ " + streamId;
                String response = Connector.getInstance().execute(dataHeader);
                Utils.checkError(response);
                String text = response.substring(dataHeader.length() + 1);
                streams.get(streamId).fireTextReceived(text);
            } else if (isReceivedDatagram(dataResponse)) {
                String data = dataResponse.substring("DATAGRAM ".length());
                String streamId = data.substring(0, data.indexOf(' '));
                String datagram = data.substring(data.indexOf(' ') + 1);
                streams.get(streamId).fireDatagramReceived(datagram);
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

    private void fireConnected(Stream stream) {
        assert stream != null;
        ApplicationListener[] listeners = this.listeners.toArray(new ApplicationListener[0]); // イベント通知中にリストが変更される可能性があるため
        for (ApplicationListener listener : listeners) {
            listener.connected(stream);
        }
    }

    void fireDisconnected(Stream stream) {
        assert stream != null;
        ApplicationListener[] listeners = this.listeners.toArray(new ApplicationListener[0]); // イベント通知中にリストが変更される可能性があるため
        for (ApplicationListener listener : listeners) {
            listener.disconnected(stream);
        }
    }

    public String getName() {
        return name;
    }

    public Stream connect(final Friend friend) throws SkypeException {
        Utils.checkNotNull("friend", friend);
        try {
            final String[] id = new String[1];
            final String[] error = new String[1];
            MessageProcessor processor = new MessageProcessor() {
                public void messageReceived(String message) {
                    if (message.equals("APPLICATION " + getName() + " CONNECTING " + friend.getId())) {
                    } else if (message.equals("APPLICATION " + getName() + " CONNECTING ")) {
                    } else if (message.startsWith("APPLICATION " + getName() + " STREAMS ")) {
                        id[0] = message.substring(("APPLICATION " + getName() + " STREAMS ").length());
                        releaseLock();
                    } else if (message.startsWith("ERROR ")) {
                        error[0] = message.substring("ERROR ".length());
                        releaseLock();
                    }
                }
            };
            Connector.getInstance().execute("ALTER APPLICATION " + getName() + " CONNECT " + friend.getId(), processor);
            Utils.checkError(error[0]);
            return streams.get(id[0]);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    public void addApplicationListener(ApplicationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        listeners.add(listener);
    }

    public void removeApplicationListener(ApplicationListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        listeners.remove(listener);
    }

    private class DataListener implements ConnectorListener {
        public void messageReceived(final String message) {
            final String dataHeader = "APPLICATION " + getName() + " ";
            if (isStreamMessage(message)) {
                String streamIds = message.substring(("APPLICATION " + getName() + " STREAMS ").length());
                for (String streamId: streamIds.split(" ")) {
                    if (streams.containsKey(streamId)) {
                        continue;
                    }
                    int delimiterIndex = streamId.indexOf(':');
                    String friendId = streamId.substring(0, delimiterIndex);
                    Friend friend;
                    try {
                        friend = Skype.getContactList().getFriend(friendId);
                    } catch (SkypeException e) {
                        throw new IllegalStateException("can't get friend: friendId = " + friendId);
                    }
                    int number = Integer.parseInt(streamId.substring(delimiterIndex + 1));
                    Stream stream = new Stream(Application.this, friend, number);
                    streams.put(streamId, stream);
                    fireConnected(stream);
                }
                NEXT: for (String existedStreamId: streams.keySet()) {
                    for (String streamId: streamIds.split(" ")) {
                        if (existedStreamId.equals(streamId)) {
                            continue;
                        }
                    }
                    fireDisconnected(streams.get(existedStreamId));
                }
            }
            if (message.startsWith(dataHeader)) {
                handleData(message.substring(dataHeader.length()));
            }
        }

        private boolean isStreamMessage(final String message) {
            String header = "APPLICATION " + getName() + " STREAMS ";
            return message.startsWith(header);
        }
    }

    public Friend[] getAllConnectableFriends() throws SkypeException {
        return getAllFriends("CONNECTABLE");
    }

    // connect(Friend)でつながるまで待機するため必要なし
// public Friend[] getAllConnectingFriends() throws SkypeException {
// return getAllFriends("CONNECTING");
// }

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
