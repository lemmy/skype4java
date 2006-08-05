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

import javax.swing.event.EventListenerList;

public abstract class Connector {
    public enum Status {
        PENDING_AUTHORIZATION, ATTACHED, REFUSED, NOT_AVAILABLE, API_AVAILABLE, NOT_RUNNING;
    }

    private static boolean useJNIConnector;
    private static Connector instance;

    public static synchronized void useJNIConnector(final boolean on) {
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
                connectorClassName = "com.skype.connector.osx.OSXConnector";
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

    /**
     * The debug output stream.
     * <p>
     * This stream is initialized by
     * <code>new PrintWriter(System.out, true)</code>.
     * </p>
     */
    private PrintWriter debugOut = new PrintWriter(System.out, true);
    private ConnectorListener debugListener;
    private Object debugFieldMutex = new Object();

    private String applicationName = "SkypeAPI4Java";

    private Status status = Status.NOT_RUNNING;
    private boolean isInitialized;

    private int connectTimeout = 10000;
    private int commandTimeout = 10000;

    private EventListenerList listeners = new EventListenerList();

    private int commandCount;

    protected Connector() {
    }

    public String getInstalledPath() {
        return "skype";
    }

    public final void setDebug(final boolean on) throws ConnectorException {
        synchronized (debugFieldMutex) {
            if (on) {
                if (debugListener == null) {
                    debugListener = new AbstractConnectorListener() {
                        @Override
                        public void messageReceived(final ConnectorMessageEvent event) {
                            getDebugOut().println("<- " + event.getMessage());
                        }
                        
                        @Override
                        public void messageSent(final ConnectorMessageEvent event) {
                            getDebugOut().println("-> " + event.getMessage());
                        }
                    };
                    addConnectorListener(debugListener);
                }
            } else {
                if (debugListener != null) {
                    removeConnectorListener(debugListener);
                }
            }
        }
    }

    /**
     * Sets the debug output stream.
     * @param debugOut the new debug output stream
     * @throws NullPointerException if <code>debugOut</code> is null.
     * @see #setDebugOut(PrintStream)
     * @see #getDebugOut()
     */
    public final void setDebugOut(final PrintWriter debugOut) {
        Utils.checkNotNull("debugOut", debugOut);
        this.debugOut = debugOut;
    }

    /**
     * Sets the debug output stream.
     * @param debugOut the new debug output stream
     * @throws NullPointerException if <code>debugOut</code> is null.
     * @see #setDebugOut(PrintWriter)
     * @see #getDebugOut()
     */
    public final void setDebugOut(final PrintStream debugOut) {
        Utils.checkNotNull("debugOut", debugOut);
        setDebugOut(new PrintWriter(debugOut, true));
    }

    /**
     * Gets the debug output stream.
     * @return the current debug output stream
     * @see #setDebugOut(PrintWriter)
     * @see #setDebugOut(PrintStream)
     */
    public final PrintWriter getDebugOut() {
        return debugOut;
    }

    public final void setApplicationName(final String applicationName) {
        Utils.checkNotNull("applicationName", applicationName);
        this.applicationName = applicationName;
    }

    public final String getApplicationName() {
        return applicationName;
    }

    protected final void setStatus(final Status newValue) {
        if (status != newValue) {
            status = newValue;
            fireStatusChanged(newValue);
        }
    }

    public final Status getStatus() {
        return status;
    }

    public final void setConnectTimeout(final int newValue) {
        connectTimeout = newValue;
    }

    public final int getConnectTimeout() {
        return connectTimeout;
    }

    public final void setCommandTimeout(final int newValue) {
        commandTimeout = newValue;
    }

    public final int getCommandTimeout() {
        return commandTimeout;
    }

    public final synchronized Status connect() throws ConnectorException {
        int timeout = getConnectTimeout();
        if (!isInitialized) {
            initialize(timeout);
            isInitialized = true;
        }
        Status status = connect(timeout);
        if (status == Status.ATTACHED) {
            try {
                sendApplicationName(getApplicationName());
                execute("PROTOCOL 9999", new String[] {"PROTOCOL "}, false);
            } catch (TimeOutException e) {
                status = Status.NOT_RUNNING;
            }
        }
        return status;
    }
    
    protected abstract void initialize(int timeout) throws ConnectorException;
    protected abstract Status connect(int timeout) throws ConnectorException;
    protected void sendApplicationName(String applicationName) throws ConnectorException {
    }

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

    public final void execute(final String command, final MessageProcessor processor) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("processor", processor);
        assureAttached();
        final Object lock = new Object();
        ConnectorListener listener = new AbstractConnectorListener() {
            public void messageReceived(ConnectorMessageEvent event) {
                processor.messageReceived(event.getMessage());
            }
        };
        processor.init(lock, listener);
        addConnectorListener(listener, false);
        fireMessageSent(command);
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

    public final String execute(final String command) throws ConnectorException {
        Utils.checkNotNull("command", command);
        return execute(command, command);
    }

    public final String executeWithId(final String command, final String responseHeader) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("responseHeader", responseHeader);
        String header = "#" + (commandCount++) + " ";
        String response = execute(header + command, new String[] { header + responseHeader, header + "ERROR " }, true);
        return response.substring(header.length());
    }

    public final String execute(final String command, final String responseHeader) throws ConnectorException {
        Utils.checkNotNull("responseHeader", responseHeader);
        return execute(command, new String[] { responseHeader, "ERROR " }, true);
    }

    public final String execute(final String command, final String[] responseHeaders) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("responseHeaders", responseHeaders);
        return execute(command, responseHeaders, true);
    }

    protected String execute(final String command, final String[] responseHeaders, final boolean checkAttached) throws ConnectorException {
        Utils.checkNotNull("command", command);
        Utils.checkNotNull("responseHeaders", responseHeaders);
        if (checkAttached) {
            assureAttached();
        }
        final Object lock = new Object();
        final String[] response = new String[1];
        ConnectorListener listener = new AbstractConnectorListener() {
            public void messageReceived(ConnectorMessageEvent event) {
                String message = event.getMessage();
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
        fireMessageSent(command);
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

    private void fireMessageSent(final String message) {
        assert message != null;
        ConnectorListener[] listeners = Connector.this.listeners.getListeners(ConnectorListener.class);
        if (listeners.length == 0) {
            return;
        }
        ConnectorMessageEvent event = new ConnectorMessageEvent(this, message);
        for (ConnectorListener listener : listeners) {
            listener.messageSent(event);
        }
    }

    protected abstract void sendCommand(String command);

    private void assureAttached() throws ConnectorException {
        Status status = getStatus();
        if (status != Status.ATTACHED) {
            status = connect();
            status = getStatus();
            if (status != Status.ATTACHED) {
            	System.err.println("Connector.assureAttached() status="+status);
                throw new NotAttachedException(status);
            }
        }
    }

    public final void addConnectorListener(final ConnectorListener listener) throws ConnectorException {
        addConnectorListener(listener, true);
    }

    protected final void addConnectorListener(final ConnectorListener listener, boolean checkAttached) throws ConnectorException {
        Utils.checkNotNull("listener", listener);
        listeners.add(ConnectorListener.class, listener);
        if (checkAttached) {
            assureAttached();
        }
    }

    public final void removeConnectorListener(final ConnectorListener listener) {
        Utils.checkNotNull("listener", listener);
        listeners.remove(ConnectorListener.class, listener);
    }

    protected final void fireMessageReceived(final String message) {
        Utils.checkNotNull("message", message);
        new Thread("MessageSender") {
            public void run() {
                ConnectorListener[] listeners = Connector.this.listeners.getListeners(ConnectorListener.class);
                if (listeners.length == 0) {
                    return;
                }
                ConnectorMessageEvent event = new ConnectorMessageEvent(this, message);
                for (int i = listeners.length - 1; 0 <= i; i--) {
                    listeners[i].messageReceived(event);
                }
            };
        }.start();
    }

    protected final void fireStatusChanged(final Status status) {
        Utils.checkNotNull("status", status);
        new Thread("StatusSender") {
            public void run() {
                ConnectorListener[] listeners = Connector.this.listeners.getListeners(ConnectorListener.class);
                if (listeners.length == 0) {
                    return;
                }
                ConnectorStatusEvent event = new ConnectorStatusEvent(this, status);
                for (int i = listeners.length - 1; 0 <= i; i--) {
                    listeners[i].statusChanged(event);
                }
            };
        }.start();
    }
}
