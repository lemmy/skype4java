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

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

public final class Win32Connector extends Connector {
    private static final int ATTACH_SUCCESS = 0;
    private static final int ATTACH_PENDING_AUTHORIZATION = 1;
    private static final int ATTACH_REFUSED = 2;
    private static final int ATTACH_NOT_AVAILABLE = 3;
    private static final int ATTACH_API_AVAILABLE = 0x8001;
    private static final String CONNECTOR_STATUS_CHANGED_MESSAGE = "ConnectorStatusChanged";

    private static Win32Connector myInstance_ = null;

    public static synchronized Connector getInstance() {
        if(myInstance_ == null) {
            myInstance_ = new Win32Connector();
        }
        return (Connector) myInstance_;
    }

    Thread thread_ = null;

    private Win32Connector() {
    }

    @Override
    public String getInstalledPath() {
        return jni_getInstalledPath();
    }

    @Override
    protected void initialize(int timeout) {
        // Loading DLL
        System.loadLibrary("JNIConnnector");

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

    @Override
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

    @Override
    protected void sendApplicationName(final String applicationName) throws ConnectorException {
        String command = "NAME " + applicationName;
        execute(command, new String[] {command}, false);
    }

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
                break;
        }
        fireMessageReceived(CONNECTOR_STATUS_CHANGED_MESSAGE);
    }

    public void jni_onSkypeMessage(String message) {
        fireMessageReceived(message);
    }

    @Override
    protected void disposeImpl() {
        // TODO WindowsConnector#disposeImpl()
        throw new UnsupportedOperationException("WindowsConnector#disposeImpl() is not implemented yet.");
    }

    @Override
    protected void sendCommand(final String command) {
        jni_sendMessage(command);
    }

    // for native
    private native void jni_init();
    private native void jni_windowProc();
    private native void jni_sendMessage(String message);
    private native void jni_connect();
    private native String jni_getInstalledPath();
}
