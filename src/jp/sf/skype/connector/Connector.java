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
package jp.sf.skype.connector;

import java.util.ArrayList;
import java.util.List;
import jp.sf.skype.connector.win32.Win32Connector;

public abstract class Connector {
    public enum Status {
        ATTACHED, REFUSED, NOT_AVAILABLE, PENDING_AUTHORIZATION, NOT_RUNNING;
    }

    private static Connector instance;

    public static synchronized Connector getInstance() {
        if (instance == null) {
            instance = new Win32Connector();
        }
        return instance;
    }
    private List<ConnectorListener> listeners = new ArrayList<ConnectorListener>();
    private int commandResponseTime = 10000;
    private int commandCount;
    private boolean debug = false;
    private ConnectorListener debugListener;

    protected Connector() {
    }

    public final void setDebug(boolean on) throws ConnectorException {
        debug = on;
        if (debug) {
            debugListener = new ConnectorListener() {
                public void messageReceived(String message) {
                    System.out.println("<- " + message);
                }
            };
            addConnectorListener(debugListener);
        } else {
            removeConnectorListener(debugListener);
            debugListener = null;
        }
    }

    private boolean isDebug() {
        return debug;
    }

    public final void setCommandResposeTime(int newValue) {
        commandResponseTime = newValue;
    }

    public final int getCommandResponseTime() {
        return commandResponseTime;
    }

    protected abstract Status connect(int timeOut) throws ConnectorException;

    public abstract Status getStatus();

    protected abstract void sendCommand(String command);

    public abstract String getInstalledPath();

    public final boolean isRunning() throws ConnectorException {
        return connect(getCommandResponseTime()) != Status.NOT_RUNNING;
    }

    public final Status connect() throws ConnectorException {
        return connect(60000);
    }

    public final void execute(String command, final MessageProcessor processor) throws ConnectorException {
        assureAttached();
        final Object lock = new Object();
        ConnectorListener listener = new ConnectorListener() {
            public void messageReceived(String message) {
                processor.messageReceived(message);
            }
        };
        processor.init(lock, listener);
        addConnectorListener(listener, false);
        synchronized (lock) {
            if (isDebug()) {
                System.out.println("-> " + command);
            }
            sendCommand(command);
            try {
                lock.wait(getCommandResponseTime());
            } catch (InterruptedException e) {
                throw new TimeOutException("'" + command + "' is not executed");
            } finally {
                removeConnectorListener(listener);
            }
        }
    }

    public final String execute(String command) throws ConnectorException {
        return execute(command, command);
    }

    public final String executeWithId(String command, String responseHeader) throws ConnectorException {
        String header = "#" + (commandCount++) + " ";
        String response = execute(header + command, new String[] { header + responseHeader, header + "ERROR " }, true);
        return response.substring(header.length());
    }

    public final String execute(String command, String responseHeader) throws ConnectorException {
        return execute(command, new String[] { responseHeader, "ERROR " }, true);
    }

    private String execute(String command, final String responseHeader, boolean checkAttached) throws ConnectorException {
        return execute(command, new String[] { responseHeader }, checkAttached);
    }

    public final String execute(String command, String[] responseHeaders) throws ConnectorException {
        return execute(command, responseHeaders, true);
    }

    private String execute(String command, final String[] responseHeaders, boolean checkAttached) throws ConnectorException {
        if (checkAttached) {
            assureAttached();
        }
        final Object lock = new Object();
        final String[] response = new String[1];
        ConnectorListener listener = new ConnectorListener() {
            public void messageReceived(String message) {
                for (String responseHeader : responseHeaders) {
                    if (message.startsWith(responseHeader)) {
                        response[0] = message;
                        synchronized (lock) {
                            lock.notify();
                        }
                        return;
                    }
                }
            }
        };
        addConnectorListener(listener, false);
        synchronized (lock) {
            try {
                if (isDebug()) {
                    System.out.println("-> " + command);
                }
                sendCommand(command);
                lock.wait(commandResponseTime);
            } catch (InterruptedException e) {
                throw new TimeOutException("'" + command + "' is not executed");
            } finally {
                removeConnectorListener(listener);
            }
        }
        return response[0];
    }

    private void assureAttached() throws ConnectorException {
        if (getStatus() != Status.ATTACHED || !ping()) {
            connect();
        }
        if (getStatus() != Status.ATTACHED) {
            throw new NotAttachedException();
        }
    }

    private boolean ping() throws ConnectorException {
        execute("PING", "PONG", false);
        return true;
    }

    public final void addConnectorListener(ConnectorListener listener) throws ConnectorException {
        addConnectorListener(listener, true);
    }

    protected final void addConnectorListener(ConnectorListener listener, boolean checkAttached) throws ConnectorException {
        if (checkAttached) {
            assureAttached();
        }
        Utils.checkNotNull("listener", listener);
        listeners.add(listener);
    }

    public final void removeConnectorListener(ConnectorListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(listener);
    }

    protected final void fireMessageReceived(final String message) {
        assert message != null;
        new Thread("Win32MessageSender") {
            public void run() {
                ConnectorListener[] listeners = Connector.this.listeners.toArray(new ConnectorListener[0]); // イベント通知中にリストが変更される可能性があるため
                for (ConnectorListener listener : listeners) {
                    listener.messageReceived(message);
                }
            };
        }.start();
    }
}
