/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.connector.test;

import java.util.ArrayList;
import java.util.List;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorMessageEvent;

public final class TestConnector extends Connector {
    private static class Holder {
        static final TestConnector instance = new TestConnector();
    }

    public static TestConnector getInstance() {
        // http://en.wikipedia.org/wiki/Initialization_on_Demand_Holder_Idiom
        return Holder.instance;
    }

    private Object recordingFieldsMutex = new Object();
    private ConnectorListener recordListener;
    private List<Recorder> recorderList;
    private Recorder[] recorders = new Recorder[0];

    private Object playingFieldsMutex = new Object();
    private Player player;
    private ConnectorListener playingLister;
    private volatile boolean playerCleared;
    private Thread playerThread;
    private Object sentMessageFieldsMutex = new Object();
    private String sentMessage = null;

    private TestConnector() {
    }

    public void addRecorder(final Recorder recorder) throws ConnectorException {
        synchronized(recordingFieldsMutex) {
            Connector.getInstance().connect();
            if(recordListener == null) {
                recordListener = new AbstractConnectorListener() {
                    @Override
                    public void messageSent(ConnectorMessageEvent event) {
                        long time = System.currentTimeMillis();
                        Recorder[] recorders = TestConnector.this.recorders;
                        for(Recorder recorder: recorders) {
                            recorder.recordSentMessage(time - recorder.getStartTime(), event.getMessage());
                        }
                    }

                    @Override
                    public void messageReceived(ConnectorMessageEvent event) {
                        long time = System.currentTimeMillis();
                        Recorder[] recorders = TestConnector.this.recorders;
                        for(Recorder recorder: recorders) {
                            recorder.recordReceivedMessage(time - recorder.getStartTime(), event.getMessage());
                        }
                    }
                };
                Connector.getInstance().addConnectorListener(recordListener);
            }
            if(recorderList == null) {
                recorderList = new ArrayList<Recorder>();
            }
            if(!recorderList.contains(recorder)) {
                recorderList.add(recorder);
                recorders = recorderList.toArray(new Recorder[0]);
                if(!recorder.isStarted()) {
                    recorder.setStartTime(System.currentTimeMillis());
                }
            }
        }
    }

    public void removeRecorder(final Recorder recorder) throws ConnectorException {
        synchronized(recordingFieldsMutex) {
            if(!recorderList.contains(recorder)) {
                return;
            }
            recorderList.remove(recorder);
            recorders = recorderList.toArray(new Recorder[0]);
            if(recorderList.isEmpty()) {
                recorderList = null;
                Connector.getInstance().removeConnectorListener(recordListener);
                recordListener = null;
            }
        }
    }

    public void setPlayer(final Player player) throws ConnectorException {
        synchronized(playingFieldsMutex) {
            Connector.setInstance(this);
            Connector.getInstance().connect();
            if(this.player != null) {
                clearPlayer();
            }
            this.player = player;
            playingLister = new AbstractConnectorListener() {
                @Override
                public void messageSent(ConnectorMessageEvent event) {
                    synchronized(sentMessageFieldsMutex) {
                        while (sentMessage != null) {
                            try {
                                sentMessageFieldsMutex.wait();
                            } catch(InterruptedException e) {
                                return;
                            }
                        }
                        sentMessage = event.getMessage();
                        sentMessageFieldsMutex.notify();
                    }
                }
            };
            Connector.getInstance().addConnectorListener(playingLister);
            playerCleared = false;
            playerThread = new Thread("TestConnectorPlayer") {
                @Override
                public void run() {
                    try {
                        player.init();
                        long startTime = System.currentTimeMillis();
                        while (player.hasNextMessage()) {
                            PlayerMessage message = player.getNextMessage();
                            switch (message.getType()) {
                                case SENT:
                                    synchronized(sentMessageFieldsMutex) {
                                        while (sentMessage == null) {
                                            try {
                                                sentMessageFieldsMutex.wait();
                                            } catch(InterruptedException e) {
                                                if (playerCleared) {
                                                    return;
                                                }
                                            }
                                        }
                                        String sentMessage = TestConnector.this.sentMessage;
                                        TestConnector.this.sentMessage = null;
                                        sentMessageFieldsMutex.notify();
                                        if (!message.getMessage().equals(sentMessage)) {
                                            throw new IllegalStateException("The sent message (=\"" + sentMessage + "\")is not equal to the expected message (=\"" + message.getMessage() + "\").");
                                        }
                                    }
                                    break;
                                case RECEIVED:
                                    long period = System.currentTimeMillis() - startTime - message.getTime(); 
                                    if (0 < period) {
                                        try {
                                            Thread.sleep(period);
                                        } catch(InterruptedException e) {
                                            if (playerCleared) {
                                                return;
                                            }
                                        }
                                    }
                                    fireMessageReceived(message.getMessage());
                                    break;
                            }
                            if (playerCleared) {
                                return;
                            }
                        }
                    } finally {
                        player.destory();
                    }
                }
            };
            playerThread.setDaemon(true);
            playerThread.start();
        }
    }

    public void clearPlayer() throws ConnectorException {
        synchronized(playingFieldsMutex) {
            playerCleared = true;
            playerThread.interrupt();
            playerThread = null;
            Connector.getInstance().removeConnectorListener(playingLister);
            playingLister = null;
            player = null;
            Connector.setInstance(null);
        }
    }

    @Override
    protected void initialize(int timeout) throws ConnectorException {
        setStatus(Status.ATTACHED);
    }

    @Override
    protected Status connect(int timeout) throws ConnectorException {
        return getStatus();
    }

    @Override
    protected void sendCommand(String command) {
        if (command.equals("PROTOCOL 9999")) {
            fireMessageReceived("PROTOCOL 6");
        }
    }

    @Override
    protected void disposeImpl() {
    }
}
