/*******************************************************************************
 * Copyright (c) 2006 r-yu/xai
 *
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * r-yu/xai - initial implementation
 * Koji Hisano - changed Skype event dispatch thread to a deamon thread
 ******************************************************************************/
package com.skype.connector.win32;

import java.io.File;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorUtils;

/**
 * Implementation of a connector for Windows.
 * This implementation uses a small dll to connect.
 * The WindowsConnector uses SWT library.
 * Choose wisely.
 */
public final class Win32Connector extends Connector {
    /** Status ATTACH_SUCCES value. */
	private static final int ATTACH_SUCCESS = 0;
	/** Status ATTACH_PENDING_AUTHORISATION value. */
    private static final int ATTACH_PENDING_AUTHORIZATION = 1;
    /** Status ATTACH_REFUSED value. */
    private static final int ATTACH_REFUSED = 2;
    /** Status ATTACH_NOT_AVAILABLE value. */
    private static final int ATTACH_NOT_AVAILABLE = 3;
    /** Status ATTACH_API_AVAILABLE value. */
    private static final int ATTACH_API_AVAILABLE = 0x8001;
    /** Filename of the DLL. */
    private static final String LIBFILENAME = "JNIConnnector.dll";
    
    /** Singleton instance. */
    private static Win32Connector myInstance_ = null;

    /**
     * Get singleton instance.
     * @return instance.
     */
    public static synchronized Connector getInstance() {
        if(myInstance_ == null) {
            myInstance_ = new Win32Connector();
        }
        return (Connector) myInstance_;
    }

    /** Thread. */
    private Thread thread_ = null;

    /**
     * Constructor.
     *
     */
    private Win32Connector() {
    }

    /**
     * Return the path of Skype.exe.
     * @return absolute path to Skype.exe.
     */
    public String getInstalledPath() {
        return jni_getInstalledPath();
    }

    /**
     * Initialize the connector.
     * @param timeout maximum time in miliseconds to initialize.
     */
    protected void initialize(int timeout) {
        // Loading DLL
    	try {
    		System.loadLibrary("JNIConnnector");
    	} catch (Throwable e) {
    		if (!ConnectorUtils.checkLibraryInPath(LIBFILENAME)) {
	    		ConnectorUtils.extractFromJarToTemp(LIBFILENAME);   	    		
	    		System.load(System.getProperty("java.io.tmpdir")+File.separatorChar+LIBFILENAME);
			}
    	}
        // Initializing JNI
        jni_init();

        // Starting Window Thread
        thread_ = new Thread(new Runnable() {
            public void run() {
                jni_windowProc();
            }
        }, "SkypeBridge WindowProc Thread");
        thread_.setDaemon(true);
        thread_.start();
    }

    /**
     * Connect to Skype client.
     * @param timeout the maximum time in milliseconds to connect.
     * @return Status the status after connecting.
     * @throws ConnectorException when connection can not be established.
     */
    protected Status connect(int timeout) throws ConnectorException {
        try {
            while(true) {
                jni_connect();
                long start = System.currentTimeMillis();
                if(timeout <= System.currentTimeMillis() - start) {
                    setStatus(Status.NOT_RUNNING);
                }
                Status status = getStatus();
                if(status != Status.PENDING_AUTHORIZATION && status != Status.NOT_RUNNING) {
                    return status;
                }
                Thread.sleep(1000);
            }
        } catch(InterruptedException e) {
            throw new ConnectorException("Trying to connect was interrupted.", e);
        }
    }

    /**
     * Send applicationname to Skype client.
     * @param applicationName The new Application name.
     * @throws ConnectorException when Skype Client connection has gone bad. 
     */
    protected void sendApplicationName(final String applicationName) throws ConnectorException {
        String command = "NAME " + applicationName;
        execute(command, new String[] {command}, false);
    }

    /**
     * Set the connector status.
     * This method will be called by the native lib.
     * @param status The new status.
     */
    public void jni_onAttach(int status) {
        switch(status) {
            case ATTACH_PENDING_AUTHORIZATION:
                setStatus(Status.PENDING_AUTHORIZATION);
                break;
            case ATTACH_SUCCESS:
                setStatus(Status.ATTACHED);
                break;
            case ATTACH_REFUSED:
                setStatus(Status.REFUSED);
                break;
            case ATTACH_NOT_AVAILABLE:
                setStatus(Status.NOT_AVAILABLE);
                break;
            case ATTACH_API_AVAILABLE:
                setStatus(Status.API_AVAILABLE);
                break;
            default:
                setStatus(Status.NOT_RUNNING);
                break;
        }
    }

    /**
     * This method gets called when the native lib has a message received.
     * @param message The received message.
     */
    public void jni_onSkypeMessage(String message) {
        fireMessageReceived(message);
    }

    /**
     * Clean up the connector and the native lib.
     */
    protected void disposeImpl() {
        // TODO WindowsConnector#disposeImpl()
        throw new UnsupportedOperationException("WindowsConnector#disposeImpl() is not implemented yet.");
    }

    /**
     * Send a command to the Skype client.
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        jni_sendMessage(command);
    }

    // for native
    /**
     * Native init method.
     */
    private native void jni_init();
    
    /**
     * native event loop method.
     *
     */
    private native void jni_windowProc();
    
    /**
     * Native send message method.
     * @param message The message to send.
     */
    private native void jni_sendMessage(String message);
    
    /***
     * The native connect method.
     *
     */
    private native void jni_connect();
    
    /**
     * The native get installed path method.
     * @return String with the absolute path to Skype.exe.
     */
    private native String jni_getInstalledPath();
}
