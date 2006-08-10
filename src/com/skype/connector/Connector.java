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

/**
 * Base class for all platform specific connectors.
 * A connector connects the Skype Java API with a running Skype client.
 */
public abstract class Connector {

	/**
	 * Enumeration of the connector status.
	 */
	public enum Status {
		/**
		 * PENDING_AUTHORIZATION - The connector is waiting for the user to accept this app to connect to the Skype client.
		 * ATTACHED - The connector is attached to the Skype client.
		 * REFUSED - The user denied the application to connect to the Skype client.
		 * NOT_AVAILABLE - The is no Skype client available to connect to.
		 * API_AVAILABLE - Redundant of ATTACHED.
		 * NOT_RUNNING - Connection can't be established. 
		 */
        PENDING_AUTHORIZATION, ATTACHED, REFUSED, NOT_AVAILABLE, API_AVAILABLE, NOT_RUNNING;
    }

	/** useJNIConnector if this is true on windows the connection will be made using a dll instead of using swt library. */
    private static boolean useJNIConnector;
    /** Singleton instance of this class. */
    private static Connector instance;

    /**
     * To use the win32 dll instead of the SWT library please use this method.
     * @param on If true the win32 connector will be used.
     */
    public static synchronized void useJNIConnector(final boolean on) {
        if (instance != null) {
            throw new IllegalStateException("You should call this method before calling Connector#getInstance().");
        }
        useJNIConnector = on;
    }

    /**
     * Initialize a platform specific connection.
     * This method will select a connector based on the os.name.
     * Windows has two versions see useJNIConnector.
     * @return an initialized connection.
     */
    public static synchronized Connector getInstance() {
        if (instance == null) {
            String osName = System.getProperty("os.name");
            String connectorClassName = null;
            if (osName.startsWith("Windows")) {
            	//Todo: add a check to see if swt is in the classpath, if not use the other connector.
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
    
    protected static synchronized void setInstance(final Connector instance) throws ConnectorException {
        if (Connector.instance != null) {
            Connector.instance.dispose();
        }
        Connector.instance = instance;
    }

    /**
     * The debug output stream.
     * <p>
     * This stream is initialized by
     * <code>new PrintWriter(System.out, true)</code>.
     * </p>
     */
    private PrintWriter debugOut = new PrintWriter(System.out, true);
    /** debugListener. */
    private ConnectorListener debugListener;
    /** debug printer lock object. */
    private Object debugFieldMutex = new Object();
    
    /** application name to send to Skype client. */
    private String applicationName = "SkypeAPI4Java";

    /** Initialize the status of the connector. */
    private Status status = Status.NOT_RUNNING;
    /** Boolean to check if the connector is already initialized. */
    private boolean isInitialized;

    /** global connector timeout. */
    private int connectTimeout = 10000;
    /** global command-reply timeout. */
    private int commandTimeout = 10000;

    /** Collection of event listeners for the connector. */
    private EventListenerList listeners = new EventListenerList();

    /** Command counter, can be used to identify message and reply pairs. */
    private int commandCount;

    /**
     * Because this object should be a singleton the constructor is protected.
     *
     */
    protected Connector() {
    }

    /**
     * Try to get the absolute path to the skype client.
     * Should be overridden for each platfrom specific connector.
     * Not geranteed to work.
     * @return The absolute path to the Skype client executable.
     */
    public String getInstalledPath() {
		return "skype";
    }

    /**
     * Enable or disable debug printing for more information.
     * @param on if true debug output will be written to System.out
     * @throws ConnectorException thrown when connection to Skype Client has gone bad.
     */
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
     * @param newDebugOut the new debug output stream
     * throws NullPointerException if <code>debugOut</code> is null.
     * @see #setDebugOut(PrintStream)
     * @see #getDebugOut()
     */
    public final void setDebugOut(final PrintWriter newDebugOut) {
        ConnectorUtils.checkNotNull("debugOut", newDebugOut);
        this.debugOut = newDebugOut;
    }

    /**
     * Sets the debug output stream.
     * @param newDebugOut the new debug output stream
     * throws NullPointerException if <code>debugOut</code> is null.
     * @see #setDebugOut(PrintWriter)
     * @see #getDebugOut()
     */
    public final void setDebugOut(final PrintStream newDebugOut) {
        ConnectorUtils.checkNotNull("debugOut", newDebugOut);
        setDebugOut(new PrintWriter(newDebugOut, true));
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

    /**
     * Set the application name for this application.
     * This is what the User will see in the Allow/Deny dialog.
     * @param newApplicationName Name of this application.
     */
    public final void setApplicationName(final String newApplicationName) {
        ConnectorUtils.checkNotNull("applicationName", newApplicationName);
        this.applicationName = newApplicationName;
    }

    /**
     * Return the current application name.
     * @return applicationName.
     */
    public final String getApplicationName() {
        return applicationName;
    }

    /**
     * Set the status of this connector instance.
     * @param newValue The new status.
     */
    protected final void setStatus(final Status newValue) {
        if (status != newValue) {
            status = newValue;
            fireStatusChanged(newValue);
        }
    }

    /**
     * Return the status of this connector instance.
     * @return status.
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * Change the connect timeout of this connector instance.
     * @param newValue the new timeout value in milliseconds.
     */
    public final void setConnectTimeout(final int newValue) {
        connectTimeout = newValue;
    }

    /**
     * Return the current connect timeout settings of the connector instance.
     * @return current connect timeout.
     */
    public final int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Change the command timeout value.
     * @param newValue The new timeout value in milliseconds.
     */
    public final void setCommandTimeout(final int newValue) {
        commandTimeout = newValue;
    }

    /**
     * Return the current command timeout setting.
     * @return command timeout value.
     */
    public final int getCommandTimeout() {
        return commandTimeout;
    }

    /**
     * Connect the connector instance to the Skype client.
     * @return the status after connecting.
     * @throws ConnectorException thrown when a connection could not be made due to technical problems.
     */
    public final synchronized Status connect() throws ConnectorException {
        int timeout = getConnectTimeout();
        if (!isInitialized) {
            initialize(timeout);
            isInitialized = true;
        }
        Status tmpStatus = connect(timeout);
        if (tmpStatus == Status.ATTACHED) {
            try {
                sendApplicationName(getApplicationName());
                execute("PROTOCOL 9999", new String[] {"PROTOCOL "}, false);
            } catch (TimeOutException e) {
                tmpStatus = Status.NOT_RUNNING;
            }
        }
        return tmpStatus;
    }
    
    /**
     * Platform specific connector needs to implement it's own initialize method.
     * @param timeout Timeout in milliseconds to use while initializing.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    protected abstract void initialize(int timeout) throws ConnectorException;
    
    /**
     * Platform specific connector needs to implement it's own connect method. 
     * @param timeout Timeout is milliseconds to use while connecting.
     * @return Status after connecting.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    protected abstract Status connect(int timeout) throws ConnectorException;
    
    /**
     * Clean-up and disconnect from Skype client.
     */
    protected abstract void disposeImpl();
    
    /**
     * Send the application name to the Skype client.
     * todo: should this be abstract?
     * @param newApplicationName new application name.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    protected void sendApplicationName(String newApplicationName) throws ConnectorException {
    }

    /**
     * Clean up this connection instance and the platform specific one.
     * IMPORTANT!
     * This allows all native code to clean up and disconnect in a nice way.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final synchronized void dispose() throws ConnectorException {
        if (!isInitialized) {
            return;
        }
        disposeImpl();
        isInitialized = false;
    }

    
    /**
     * Check if connector is connected to the Skype Client.
     * @return true if connector is connected.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
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

    /**
     * Send a Skype command to the Skype client and wait for the reply.
     * This method is not event-save. another reply could be picked-up.
     * Please use executeWithID or execute with responseheader instead.
     * @param command the command to send.
     * @return the reply message.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final String execute(final String command) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        return execute(command, command);
    }

    /**
     * Send a Skype command to the Skype client and wait for the reply, using an ID.
     * @param command The command to send.
     * @param responseHeader The expected reply header.
     * @return The reply.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final String executeWithId(final String command, final String responseHeader) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        ConnectorUtils.checkNotNull("responseHeader", responseHeader);
        String header = "#" + (commandCount++) + " ";
        String response = execute(header + command, new String[] { header + responseHeader, header + "ERROR " }, true);
        return response.substring(header.length());
    }

    /**
     * Send a Skype command to the Skype client and wait for the reply based on the responseheader.
     * @param command the command to send.
     * @param responseHeader the expected reply header.
     * @return the reply.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final String execute(final String command, final String responseHeader) throws ConnectorException {
        ConnectorUtils.checkNotNull("responseHeader", responseHeader);
        return execute(command, new String[] { responseHeader, "ERROR " }, true);
    }

    /**
     * Send a Skype command to Skype client and allow for several reply headers.
     * @param command the command to send.
     * @param responseHeaders the expected response headers.
     * @return the reply.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final String execute(final String command, final String[] responseHeaders) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        ConnectorUtils.checkNotNull("responseHeaders", responseHeaders);
        return execute(command, responseHeaders, true);
    }

    /**
     * Send a Skype command to Skype (actual implementation method) and wait for response.
     * @param command the command to send.
     * @param responseHeaders The expected response headers.
     * @param checkAttached if true the connector will first check if it is connected.
     * @return the response.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    protected final String execute(final String command, final String[] responseHeaders, final boolean checkAttached) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        ConnectorUtils.checkNotNull("responseHeaders", responseHeaders);
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

    /**
     * Event trigger called when a command is send to the Skype client.
     * @param message the message that has been send.
     */
    private void fireMessageSent(final String message) {
        assert message != null;
        ConnectorListener[] fireListeners = Connector.this.listeners.getListeners(ConnectorListener.class);
        if (fireListeners.length == 0) {
            return;
        }
        ConnectorMessageEvent event = new ConnectorMessageEvent(this, message);
        for (ConnectorListener listener : fireListeners) {
            listener.messageSent(event);
        }
    }

    /**
     * Send a command message to the Skype client.
     * @param command the command message to send.
     */
    protected abstract void sendCommand(String command);

    /**
     * Method to check the attached status of the connector to the Skype Client.
     * If it isn't connected it will connect.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    private void assureAttached() throws ConnectorException {
        Status attachedStatus = getStatus();
        if (attachedStatus != Status.ATTACHED) {
            attachedStatus = connect();
            attachedStatus = getStatus();
            if (attachedStatus != Status.ATTACHED) {
            	System.err.println("Connector.assureAttached() status="+attachedStatus);
                throw new NotAttachedException(attachedStatus);
            }
        }
    }

    /**
     * Add a listener to this connector instance.
     * @param listener the listener to add.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final void addConnectorListener(final ConnectorListener listener) throws ConnectorException {
        addConnectorListener(listener, true);
    }

    /**
     * Add a listener to this connector if the connector is attached.
     * @param listener The listener to add.
     * @param checkAttached if true check if connector is attached.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    protected final void addConnectorListener(final ConnectorListener listener, boolean checkAttached) throws ConnectorException {
        ConnectorUtils.checkNotNull("listener", listener);
        listeners.add(ConnectorListener.class, listener);
        if (checkAttached) {
            assureAttached();
        }
    }

    /**
     * Remove a listener from the collection of listeners. The listener will no longer be triggered when a event happens.
     * @param listener The listener to remove.
     */
    public final void removeConnectorListener(final ConnectorListener listener) {
        ConnectorUtils.checkNotNull("listener", listener);
        listeners.remove(ConnectorListener.class, listener);
    }

    /**
     * Fire a message received event.
     * @param message the message that triggered the event.
     */
    protected final void fireMessageReceived(final String message) {
        ConnectorUtils.checkNotNull("message", message);
        new Thread("MessageSender") {
            public void run() {
                ConnectorListener[] fireListeners = Connector.this.listeners.getListeners(ConnectorListener.class);
                if (fireListeners.length == 0) {
                    return;
                }
                ConnectorMessageEvent event = new ConnectorMessageEvent(this, message);
                for (int i = fireListeners.length - 1; 0 <= i; i--) {
                    fireListeners[i].messageReceived(event);
                }
            };
        }.start();
    }

    /**
     * Fire a status change event.
     * @param newStatus the new status that triggered this event.
     */
    protected final void fireStatusChanged(final Status newStatus) {
        ConnectorUtils.checkNotNull("status", newStatus);
        new Thread("StatusSender") {
            public void run() {
                ConnectorListener[] fireListeners = Connector.this.listeners.getListeners(ConnectorListener.class);
                if (fireListeners.length == 0) {
                    return;
                }
                ConnectorStatusEvent event = new ConnectorStatusEvent(this, newStatus);
                for (int i = fireListeners.length - 1; 0 <= i; i--) {
                    fireListeners[i].statusChanged(event);
                }
            };
        }.start();
    }
}
