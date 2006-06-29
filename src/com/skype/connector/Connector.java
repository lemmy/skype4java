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
 * Bart Lamot - changed package and class of the MacOS to OSX
 ******************************************************************************/
package com.skype.connector;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Connector {
    public enum Status {
        PENDING_AUTHORIZATION, ATTACHED, REFUSED, NOT_AVAILABLE, API_AVAILABLE, NOT_RUNNING;
    }

    private static boolean useJNIConnector;
    private static Connector instance;

    public static synchronized void useJNIConnector(boolean on) {
        if (instance != null) {
            throw new IllegalStateException("You should call this method before calling Connector#getInstance().");
        }
        useJNIConnector = on;
    }

    public static synchronized Connector getInstance() {
        if (instance == null) {
            String osName = System.getProperty("os.name");
            String connectorClassName = null;
            if (osName.startsWith("Windows")) {
                if (useJNIConnector) {
                    connectorClassName = "com.skype.connector.win32.Win32Connector";
                } else {
                    connectorClassName = "com.skype.connector.windows.WindowsConnector";
                }
            } else if (osName.startsWith("Linux") || osName.startsWith("LINUX")) {
                connectorClassName = "com.skype.connector.linux.LinuxConnector";
            } else if (osName.startsWith("Mac OS X")) {
                connectorClassName = "jp.sf.skype.connector.osx.OSXConnector";
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
    /**
     * The debug output stream.
     * <p>
     * This stream is initialized by
     * <code>new PrintWriter(System.err, true)</code>.
     * </p>
     */
    private PrintWriter debugOut = new PrintWriter(System.err, true);
    private ConnectorMessageReceivedListener debugListener;
    private Object debugFieldMutex = new Object();

    private String applicationName = "Skype API for Java";

    private Status status = Status.NOT_RUNNING;
    private boolean isInitialized;

    private int connectTimeout = 10000;
    private int commandTimeout = 10000;

    private List<ConnectorMessageReceivedListener> messageReceivedListeners = Collections.synchronizedList(new ArrayList<ConnectorMessageReceivedListener>());
    private List<ConnectorStatusChangedListener> statusChangedListeners = Collections.synchronizedList(new ArrayList<ConnectorStatusChangedListener>());

    private int commandCount;

    protected Connector() {
    }

    public final void setDebug(boolean on) throws ConnectorException {
        synchronized (debugFieldMutex) {
            if (debug == on) {
                return;
            }
            debug = on;
            if (debug) {
                debugListener = new ConnectorMessageReceivedListener() {
                    public void messageReceived(String message) {
                        getDebugOut().println("<- " + message);
                    }
                };
                addConnectorMessageReceivedListener(debugListener);
            } else {
                removeConnectorMessageReceivedListener(debugListener);
                debugListener = null;
            }
        }
    }

    private boolean isDebug() {
        return debug;
    }

    /**
     * Sets the debug output stream.
     * 
     * @param debugOut
     *            the new debug output stream
     * 
     * @throws NullPointerException
     *             if <code>debugOut</code> is null.
     * 
     * @see #setDebugOut(PrintStream)
     * @see #getDebugOut()
     */
    public final void setDebugOut(PrintWriter debugOut) {
        Utils.checkNotNull("debugOut", debugOut);
        this.debugOut = debugOut;
    }

    /**
     * Sets the debug output stream.
     * 
     * @param debugOut
     *            the new debug output stream
     * 
     * @throws NullPointerException
     *             if <code>debugOut</code> is null.
     * 
     * @see #setDebugOut(PrintWriter)
     * @see #getDebugOut()
     */
    public final void setDebugOut(PrintStream debugOut) {
        Utils.checkNotNull("debugOut", debugOut);
        setDebugOut(new PrintWriter(debugOut, true));
    }

    /**
     * Gets the debug output stream.
     * 
     * @return the current debug output stream
     * 
     * @see #setDebugOut(PrintWriter)
     * @see #setDebugOut(PrintStream)
     */
    public final PrintWriter getDebugOut() {
        return debugOut;
    }

    public final void setApplicationName(String applicationName) {
        Utils.checkNotNull("applicationName", applicationName);
        this.applicationName = applicationName;
    }

    public final String getApplicationName() {
        return applicationName;
    }

    protected final void setStatus(Status newStatus) {
        if (status != newStatus) {
            status = newStatus;
            fireStatusChanged(newStatus);
        }
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

    public final synchronized Status connect() throws ConnectorException {
        int timeout = getConnectTimeout();
        if (!isInitialized) {
            initialize(timeout);
            isInitialized = true;
        }
        Status status = connectImpl(timeout);
        if (status == Status.ATTACHED) {
            try {
                execute("PROTOCOL 9999", new String[] { "PROTOCOL " }, false);
                execute("NAME " + getApplicationName(), new String[] { "NAME " + getApplicationName() }, false);
            } catch (TimeOutException e) {
                status = Status.NOT_RUNNING;
            }
        }
        return status;
    }

    protected abstract void initialize(int timeout) throws ConnectorException;

    protected abstract Status connectImpl(int timeout) throws ConnectorException;

    public final synchronized void dispose() throws ConnectorException {
        if (!isInitialized) {
            return;
        }
        disposeImpl();
        isInitialized = false;
    }

    protected abstract void disposeImpl();

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
        ConnectorMessageReceivedListener listener = new ConnectorMessageReceivedListener() {
            public void messageReceived(String message) {
                processor.messageReceived(message);
            }
        };
        processor.init(lock, listener);
        addConnectorMessageReceivedListener(listener, false);
        if (isDebug()) {
            getDebugOut().println("-> " + command);
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
                removeConnectorMessageReceivedListener(listener);
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

    protected String execute(String command, final String[] responseHeaders, boolean checkAttached) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("responseHeaders", responseHeaders);
        if (checkAttached) {
            assureAttached();
        }
        final Object lock = new Object();
        final String[] response = new String[1];
        ConnectorMessageReceivedListener listener = new ConnectorMessageReceivedListener() {
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
        addConnectorMessageReceivedListener(listener, false);
        if (isDebug()) {
            getDebugOut().println("-> " + command);
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
                removeConnectorMessageReceivedListener(listener);
            }
        }
        return response[0];
    }

    protected abstract void sendCommand(String command);

    private void assureAttached() throws ConnectorException {
        Status status = getStatus();
        if (status != Status.ATTACHED) {
            status = connect();
            if (status != Status.ATTACHED) {
                throw new NotAttachedException(status);
            }
        }
    }

    public final void addConnectorMessageReceivedListener(ConnectorMessageReceivedListener listener) throws ConnectorException {
        addConnectorMessageReceivedListener(listener, true);
    }

    protected final void addConnectorMessageReceivedListener(ConnectorMessageReceivedListener listener, boolean checkAttached) throws ConnectorException {
        Utils.checkNotNull("listener", listener);
        messageReceivedListeners.add(listener);
        if (checkAttached) {
            assureAttached();
        }
    }

    public final void removeConnectorMessageReceivedListener(ConnectorMessageReceivedListener listener) {
        Utils.checkNotNull("listener", listener);
        messageReceivedListeners.remove(listener);
    }

    protected final void fireMessageReceived(final String message) {
        assert message != null;
        new Thread("MessageSender") {
            public void run() {
                ConnectorMessageReceivedListener[] listeners = messageReceivedListeners.toArray(new ConnectorMessageReceivedListener[0]);
                for (ConnectorMessageReceivedListener listener : listeners) {
                    listener.messageReceived(message);
                }
            };
        }.start();
    }

    public final void addConnectorStatusChangedListener(ConnectorStatusChangedListener listener) throws ConnectorException {
        Utils.checkNotNull("listener", listener);
        statusChangedListeners.add(listener);
    }

    public final void removeConnectorStatusChangedListener(ConnectorStatusChangedListener listener) {
        Utils.checkNotNull("listener", listener);
        statusChangedListeners.remove(listener);
    }

    protected final void fireStatusChanged(final Status status) {
        assert status != null;
        new Thread("StatusSender") {
            public void run() {
                ConnectorStatusChangedListener[] listeners = statusChangedListeners.toArray(new ConnectorStatusChangedListener[0]);
                for (ConnectorStatusChangedListener listener : listeners) {
                    listener.statusChanged(status);
                }
            };
        }.start();
    }
}
