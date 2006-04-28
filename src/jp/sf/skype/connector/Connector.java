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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Connector {
    public enum Status {
        ATTACHED, REFUSED, NOT_AVAILABLE, PENDING_AUTHORIZATION, NOT_RUNNING;
    }

    private static Connector instance;

    public static synchronized Connector getInstance() {
        if (instance == null) {
            String osName = System.getProperty("os.name");
            String connectorClassName = null;
            if (osName.startsWith("Windows")) {
                connectorClassName = "jp.sf.skype.connector.windows.WindowsConnector";
            } else if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
                connectorClassName = "jp.sf.skype.connector.linux.LinuxConnector";
            } else if (osName.startsWith("Mac OS X")) {
                connectorClassName = "jp.sf.skype.connector.macos.MacOSConnector";
            }
            if (connectorClassName == null) {
                throw new IllegalStateException("This platform is not supported by Skype API for Java.");
            }
            try {
                Class connectorClass = Class.forName(connectorClassName);
                Method getInstance = connectorClass.getMethod("getInstance");
                instance = (Connector) getInstance.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("The connector couldn't be initialized.", e);
            }
        }
        return instance;
    }

    private boolean debug = false;
    private ConnectorListener debugListener;

    private Status status = Status.NOT_RUNNING;

    private int connectTimeout = 60000;
    private int commandTimeout = 10000;
    private List<ConnectorListener> listeners = Collections.synchronizedList(new ArrayList<ConnectorListener>());
    private int commandCount;

    protected Connector() {
    }

    public final void setDebug(boolean on) throws ConnectorException {
        if (debug == on) {
            return;
        }
        debug = on;
        if (debug) {
            debugListener = new ConnectorListener() {
                public void messageReceived(String message) {
                    System.err.println("<- " + message);
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
    
    protected final void setStatus(Status status) {
        this.status = status;
    }

    public final Status getStatus() {
        return status;
    }

    public final void setConnectTimeout(int newValue) {
        connectTimeout = newValue;
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final void setCommandTimeout(int newValue) {
        commandTimeout = newValue;
    }

    public final int getCommandTimeout() {
        return commandTimeout;
    }

    public String getInstalledPath() {
        return "skype";
    }

    public final Status connect() throws ConnectorException {
        return connect(getConnectTimeout());
    }

    protected abstract Status connect(int timeout) throws ConnectorException;

    public final boolean isRunning() throws ConnectorException {
        try {
            assureAttached();
            return true;
        } catch (TimeOutException e) {
            return false;
        } catch (NotAttachedException e) {
            return false;
        }
    }

    public final void execute(String command, final MessageProcessor processor) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("processor", processor);
        assureAttached();
        final Object lock = new Object();
        ConnectorListener listener = new ConnectorListener() {
            public void messageReceived(String message) {
                processor.messageReceived(message);
            }
        };
        processor.init(lock, listener);
        addConnectorListener(listener, false);
        if (isDebug()) {
            System.err.println("-> " + command);
        }
        synchronized (lock) {
            try {
                sendCommand(command);
                long start = System.currentTimeMillis();
                long commandResponseTime = getCommandTimeout();
                lock.wait(commandResponseTime);
                if (commandResponseTime <= System.currentTimeMillis() - start) {
                    setStatus(Status.NOT_RUNNING);
                    throw new TimeOutException("The '" + command + "' command failed by timeout.");
                }
            } catch (InterruptedException e) {
                throw new ConnectorException("The '" + command + "' command was interrupted.", e);
            } finally {
                removeConnectorListener(listener);
            }
        }
    }

    public final String execute(String command) throws ConnectorException {
        return execute(command, command);
    }

    public final String executeWithId(String command, String responseHeader) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("responseHeader", responseHeader);
        String header = "#" + (commandCount++) + " ";
        String response = execute(header + command, new String[] { header + responseHeader, header + "ERROR " }, true);
        return response.substring(header.length());
    }

    public final String execute(String command, String responseHeader) throws ConnectorException {
        Utils.checkNotNull("responseHeader", responseHeader);
        return execute(command, new String[] { responseHeader, "ERROR " }, true);
    }

    public final String execute(String command, String[] responseHeaders) throws ConnectorException {
        return execute(command, responseHeaders, true);
    }

    private String execute(String command, final String[] responseHeaders, boolean checkAttached) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("responseHeaders", responseHeaders);
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
        if (isDebug()) {
            System.err.println("-> " + command);
        }
        synchronized (lock) {
            try {
                sendCommand(command);
                long start = System.currentTimeMillis();
                long commandResponseTime = getCommandTimeout();
                lock.wait(commandResponseTime);
                if (commandResponseTime <= System.currentTimeMillis() - start) {
                    setStatus(Status.NOT_RUNNING);
                    throw new TimeOutException("The '" + command + "' command failed by timeout.");
                }
            } catch (InterruptedException e) {
                throw new ConnectorException("The '" + command + "' command was interrupted.");
            } finally {
                removeConnectorListener(listener);
            }
        }
        return response[0];
    }

    protected abstract void sendCommand(String command);

    private void assureAttached() throws ConnectorException {
        long start = System.currentTimeMillis();
        int connectTimeout = getConnectTimeout();
        Status status = getStatus();
        if (status != Status.ATTACHED) {
            status = tryToConnect();
            while (status == Status.PENDING_AUTHORIZATION) {
                if (connectTimeout / 5 != 0) {
                    try {
                        Thread.sleep(connectTimeout / 5);
                    } catch (InterruptedException e) {
                        throw new ConnectorException("Trying to connect was interrupted.", e);
                    }
                }
                status = tryToConnect();
                if (connectTimeout <= System.currentTimeMillis() - start) {
                    break;
                }
            }
            if (status != Status.ATTACHED) {
                throw new NotAttachedException(status);
            }
        }
    }

    private Status tryToConnect() throws ConnectorException {
        assert status != null;
        try {
            return connect();
        } catch (TimeOutException e) {
            throw new NotAttachedException(Status.NOT_RUNNING);
        }
    }

    public final void addConnectorListener(ConnectorListener listener) throws ConnectorException {
        addConnectorListener(listener, true);
    }

    protected final void addConnectorListener(ConnectorListener listener, boolean checkAttached) throws ConnectorException {
        Utils.checkNotNull("listener", listener);
        if (checkAttached) {
            assureAttached();
        }
        listeners.add(listener);
    }

    public final void removeConnectorListener(ConnectorListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(listener);
    }

    protected final void fireMessageReceived(final String message) {
        assert message != null;
        new Thread("MessageSender") {
            public void run() {
                ConnectorListener[] listeners = Connector.this.listeners.toArray(new ConnectorListener[0]);
                for (ConnectorListener listener : listeners) {
                    listener.messageReceived(message);
                }
            };
        }.start();
    }
}
