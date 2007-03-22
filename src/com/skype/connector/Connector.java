/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static boolean _useJNIConnector;
    /** Singleton instance of this class. */
    private static Connector _instance;

    /**
     * To use the win32 dll instead of the SWT library please use this method.
     * @param on If true the win32 connector will be used.
     */
    public static synchronized void useJNIConnector(final boolean on) {
        if (_instance != null) {
            throw new IllegalStateException("You should call Connector#useJNIConnector(boolean) before calling Connector#getInstance().");
        }
        _useJNIConnector = on;
    }
    
    /**
     * Initialize a platform specific connection.
     * This method will select a connector based on the os.name.
     * Windows has two versions see useJNIConnector.
     * @return an initialized connection.
     */
    public static synchronized Connector getInstance() {
        if (_instance == null) {
            String connectorClassName = null;
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                if (!isSWTAvailable()) {
                    _useJNIConnector = true;
                }
                if (_useJNIConnector) {
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
                throw new IllegalStateException("This platform is not supported by Skype4Java.");
            }
            try {
                Class connectorClass = Class.forName(connectorClassName);
                Method getInstance = connectorClass.getMethod("getInstance");
                _instance = (Connector) getInstance.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("The connector couldn't be initialized.", e);
            }
        }
        return _instance;
    }

    /**
     * This method checks if SWT is available in the classpath.
     * @return true if SWT is found.
     */
    private static boolean isSWTAvailable() {
        try {
            Class.forName("org.eclipse.swt.SWT");
        } catch(ClassNotFoundException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Set the instance of the connector for testcases.
     * @param newInstance The new instance.
     * @throws ConnectorException thrown when instance is not valid.
     */
    protected static synchronized void setInstance(final Connector newInstance) throws ConnectorException {
        if (_instance != null) {
            _instance.dispose();
        }
        _instance = newInstance;
    }

    /**
     * The debug output stream.
     * <p>
     * This stream is initialized by
     * <code>new PrintWriter(System.out, true)</code>.
     * </p>
     */
    private PrintWriter _debugOut = new PrintWriter(System.out, true);
    /** debugListener. */
    private ConnectorListener _debugListener;
    /** debug printer lock object. */
    private Object _debugFieldMutex = new Object();
    
    /** application name to send to Skype client. */
    private String _applicationName = "Skype4Java";

    /** Initialize the status of the connector. */
    private Status _status = Status.NOT_RUNNING;
    
    /** Boolean to check if the connector is already initialized. */
    private boolean _isInitialized;
    /** initialization field mutex. */
    private Object _isInitializedMutex = new Object();

    /** global connector timeout. */
    private int _connectTimeout = 10000;
    /** global command-reply timeout. */
    private int _commandTimeout = 10000;

    /** Collection of asynchronous event listeners for the connector. */
    private ConnectorListener[] _asyncListeners = new ConnectorListener[0];
    /** Collection of synchronous event listeners for the connector. */
    private ConnectorListener[] _syncListeners = new ConnectorListener[0];

    /** Command counter, can be used to identify message and reply pairs. */
    private int _commandCount;
    
    /** Asynchronous message sender */
    private ExecutorService _asyncSender = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 20, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger();

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "AsyncSkypeMessageSender-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });
    /** Synchronous message sender */
    private Executor _syncSender = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "SyncSkypeMessageSender");
            thread.setDaemon(true);
            return thread;
        }
    });

    /**
     * Because this object should be a singleton the constructor is protected.
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
        synchronized (_debugFieldMutex) {
            if (on) {
                if (_debugListener == null) {
                    _debugListener = new AbstractConnectorListener() {
                        @Override
                        public void messageReceived(final ConnectorMessageEvent event) {
                            getDebugOut().println("<- " + event.getMessage());
                        }
                        
                        @Override
                        public void messageSent(final ConnectorMessageEvent event) {
                            getDebugOut().println("-> " + event.getMessage());
                        }
                    };
                    addConnectorListener(_debugListener, true, true);
                }
            } else {
                if (_debugListener != null) {
                    removeConnectorListener(_debugListener);
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
        this._debugOut = newDebugOut;
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
        return _debugOut;
    }

    /**
     * Set the application name for this application.
     * This is what the User will see in the Allow/Deny dialog.
     * @param newApplicationName Name of this application.
     */
    public final void setApplicationName(final String newApplicationName) {
        ConnectorUtils.checkNotNull("applicationName", newApplicationName);
        _applicationName = newApplicationName;
    }

    /**
     * Return the current application name.
     * @return applicationName.
     */
    public final String getApplicationName() {
        return _applicationName;
    }

    /**
     * Set the status of this connector instance.
     * @param newValue The new status.
     */
    protected final void setStatus(final Status newValue) {
        ConnectorUtils.checkNotNull("newValue", newValue);
        _status = newValue;
        fireStatusChanged(newValue);
    }

    /**
     * Fire a status change event.
     * @param newStatus the new status that triggered this event.
     */
    private void fireStatusChanged(final Status newStatus) {
        assert newStatus != null;
        if (_syncListeners.length != 0) {
            _syncSender.execute(new Runnable() {
                public void run() {
                    fireStatusChanged(_syncListeners, newStatus);
                }
            });
        }
        if (_asyncListeners.length != 0) {
            _asyncSender.execute(new Runnable() {
                public void run() {
                    fireStatusChanged(_asyncListeners, newStatus);
                }
            });
        }
    }

    /**
     * Fire a status changed event.
     * @param listenerList the event listener list
     * @param status the new status.
     */
    private void fireStatusChanged(final ConnectorListener[] listeners, final Status status) {
        ConnectorStatusEvent event = new ConnectorStatusEvent(this, status);
        for (int i = listeners.length - 1; 0 <= i; i--) {
            listeners[i].statusChanged(event);
        }
    }

    /**
     * Return the status of this connector instance.
     * @return status.
     */
    public final Status getStatus() {
        return _status;
    }

    /**
     * Change the connect timeout of this connector instance.
     * @param newValue the new timeout value in milliseconds.
     */
    public final void setConnectTimeout(final int newValue) {
        _connectTimeout = newValue;
    }

    /**
     * Return the current connect timeout settings of the connector instance.
     * @return current connect timeout.
     */
    public final int getConnectTimeout() {
        return _connectTimeout;
    }

    /**
     * Change the command timeout value.
     * @param newValue The new timeout value in milliseconds.
     */
    public final void setCommandTimeout(final int newValue) {
        _commandTimeout = newValue;
    }

    /**
     * Return the current command timeout setting.
     * @return command timeout value.
     */
    public final int getCommandTimeout() {
        return _commandTimeout;
    }

    /**
     * Connect the connector instance to the Skype client.
     * @return the status after connecting.
     * @throws ConnectorException thrown when a connection could not be made due to technical problems.
     */
    public final Status connect() throws ConnectorException {
        int timeout = getConnectTimeout();
        synchronized(_isInitializedMutex) {
            if (!_isInitialized) {
                initialize();
                _isInitialized = true;
            }
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
    
    /**
     * Platform specific connector needs to implement it's own initialize method.
     * @param timeout Timeout in milliseconds to use while initializing.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    protected abstract void initialize() throws ConnectorException;
    
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
    public final void dispose() throws ConnectorException {
        synchronized(_isInitializedMutex) {
            if (!_isInitialized) {
                return;
            }
            disposeImpl();
            _isInitialized = false;
        }
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
        } catch (ConnectorException e) {
            return false;
        }
    }

    /**
     * Send a Skype command to the Skype client and handle responses by a message processor.
     * This method is not event-save. another reply could be picked-up.
     * Please use executeWithID or execute with responseheader instead.
     * @param command the command to send.
     * @param processor the message processor
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    @Deprecated
    public final void execute(final String command, final MessageProcessor processor) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        ConnectorUtils.checkNotNull("processor", processor);
        assureAttached();
        final Object wait = new Object();
        ConnectorListener listener = new AbstractConnectorListener() {
            public void messageReceived(ConnectorMessageEvent event) {
                processor.messageReceived(event.getMessage());
            }
        };
        processor.init(wait, listener);
        addConnectorListener(listener, false);
        fireMessageSent(command);
        synchronized (wait) {
            try {
                sendCommand(command);
                long start = System.currentTimeMillis();
                long commandResponseTime = getCommandTimeout();
                wait.wait(commandResponseTime);
                if (commandResponseTime <= System.currentTimeMillis() - start) {
                    setStatus(Status.NOT_RUNNING);
                    throw new NotAttachedException(Status.NOT_RUNNING);
                }
            } catch (InterruptedException e) {
                throw new ConnectorException("The '" + command + "' command was interrupted.", e);
            } finally {
                removeConnectorListener(listener);
            }
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
        String header = "#" + (_commandCount++) + " ";
        String response = execute(header + command, new String[] { header + responseHeader, header + "ERROR " }, true);
        return response.substring(header.length());
    }

    /**
     * Send a Skype command to the Skype client and wait for the reply based on the responseheader without timeout.
     * @param command the command to send.
     * @param responseHeader the expected reply header.
     * @return the reply.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final String executeWithoutTimeout(final String command, final String responseHeader) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        ConnectorUtils.checkNotNull("responseHeader", responseHeader);
        return execute(command, new String[] { responseHeader, "ERROR " }, true, true);
    }

    /**
     * Send a Skype command to the Skype client and wait for the reply based on the responseheader.
     * @param command the command to send.
     * @param responseHeader the expected reply header.
     * @return the reply.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final String execute(final String command, final String responseHeader) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
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
        return execute(command, responseHeaders, checkAttached, false);
    }

    /**
     * Send a Skype command to Skype (actual implementation method) and wait for response.
     * @param command the command to send.
     * @param responseHeaders The expected response headers.
     * @param checkAttached if true the connector will first check if it is connected.
     * @param withoutTimeout if true the command has no timeout
     * @return the response.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    private String execute(final String command, final String[] responseHeaders, final boolean checkAttached, boolean withoutTimeout) throws ConnectorException {
        ConnectorUtils.checkNotNull("command", command);
        ConnectorUtils.checkNotNull("responseHeaders", responseHeaders);
        if (checkAttached) {
            assureAttached();
        }
        final Object wait = new Object();
        final String[] response = new String[1];
        ConnectorListener listener = new AbstractConnectorListener() {
            public void messageReceived(ConnectorMessageEvent event) {
                String message = event.getMessage();
                for (String responseHeader : responseHeaders) {
                    if (message.startsWith(responseHeader)) {
                        response[0] = message;
                        synchronized (wait) {
                            wait.notify();
                        }
                        return;
                    }
                }
            }
        };
        addConnectorListener(listener, false);
        synchronized (wait) {
            try {
                fireMessageSent(command);
                sendCommand(command);
                if (withoutTimeout) {
                    wait.wait();
                } else {
                    long start = System.currentTimeMillis();
                    long commandResponseTime = getCommandTimeout();
                    wait.wait(commandResponseTime);
                    if (commandResponseTime <= System.currentTimeMillis() - start) {
                        setStatus(Status.NOT_RUNNING);
                        throw new NotAttachedException(Status.NOT_RUNNING);
                    }
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
        fireMessageEvent(message, false);
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
            if (attachedStatus != Status.ATTACHED) {
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
    public final void addConnectorListener(final ConnectorListener listener, final boolean checkAttached) throws ConnectorException {
        addConnectorListener(listener, checkAttached, false);
    }

    /**
     * Add a listener to this connector if the connector is attached.
     * @param listener The listener to add.
     * @param checkAttached if true check if connector is attached.
     * @param isSynchronous if true the listener is handled synchronously.
     * @throws ConnectorException thrown when the connection to the Skype client has gone bad.
     */
    public final void addConnectorListener(final ConnectorListener listener, final boolean checkAttached, final boolean isSynchronous) throws ConnectorException {
        ConnectorUtils.checkNotNull("listener", listener);
        if (isSynchronous) {
            List<ConnectorListener> listeners = new ArrayList(Arrays.asList(_syncListeners));
            if (listeners.add(listener)) {
                _syncListeners = listeners.toArray(new ConnectorListener[0]);
            }
        } else {
            List<ConnectorListener> listeners = new ArrayList(Arrays.asList(_asyncListeners));
            if (listeners.add(listener)) {
                _asyncListeners = listeners.toArray(new ConnectorListener[0]);
            }
        }
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
        {
            List<ConnectorListener> listeners = new ArrayList(Arrays.asList(_syncListeners));
            if (listeners.remove(listener)) {
                _syncListeners = listeners.toArray(new ConnectorListener[0]);
            }
        }
        {
            List<ConnectorListener> listeners = new ArrayList(Arrays.asList(_asyncListeners));
            if (listeners.remove(listener)) {
                _asyncListeners = listeners.toArray(new ConnectorListener[0]);
            }
        }
    }

    /**
     * Fire a message received event.
     * @param message the message that triggered the event.
     */
    protected final void fireMessageReceived(final String message) {
        fireMessageEvent(message, true);
    }

    /**
     * Fire a message event.
     * @param message the message that triggered the event.
     * @param isReceived the message is a received type or not.
     */
    private void fireMessageEvent(final String message, final boolean isReceived) {
        ConnectorUtils.checkNotNull("message", message);
        if (_syncListeners.length != 0) {
            _syncSender.execute(new Runnable() {
                public void run() {
                    fireMessageEvent(_syncListeners, message, isReceived);
                }
            });
        }
        if (_asyncListeners.length != 0) {
            _asyncSender.execute(new Runnable() {
                public void run() {
                    fireMessageEvent(_asyncListeners, message, isReceived);
                }
            });
        }
    }

    /**
     * Fire a message event.
     * @param listenerList the event listener list
     * @param message the message that triggered the event.
     * @param isReceived the message is a received type or not.
     */
    private void fireMessageEvent(final ConnectorListener[] listeners, final String message, final boolean isReceived) {
        ConnectorMessageEvent event = new ConnectorMessageEvent(this, message);
        boolean fireMessageReceived = isReceived;
        for (int i = listeners.length - 1; 0 <= i; i--) {
            if (fireMessageReceived) {
                listeners[i].messageReceived(event);
            } else {
                listeners[i].messageSent(event);
            }
        }
    }
}