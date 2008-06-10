/*******************************************************************************
 * Copyright (c) 2006-2007 r-yu/xai
 *
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
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
 * r-yu/xai - initial implementation
 * Koji Hisano - changed Skype event dispatch thread to a deamon thread
 ******************************************************************************/
package com.skype.connector.osx;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorUtils;

/**
 * Implementation of the connector for Mac OS X.
 */
public final class OSXConnector extends Connector {
    private static final String LIBRARY_FILE_NAME = "JNIConnector.dll";
    
    /** Singleton instance. */
    private static OSXConnector _instance = null;

    /**
     * Get singleton instance.
     * @return instance.
     */
    public static synchronized Connector getInstance() {
        if(_instance == null) {
            _instance = new OSXConnector();
        }
        return _instance;
    }
    
    private SkypeFrameworkListener listener = new AbstractSkypeFrameworkListener() {
        @Override
        public void notificationReceived(String notificationString) {
            fireMessageReceived(notificationString);
        }
    
        @Override
        public void becameUnavailable() {
            setStatus(Status.NOT_AVAILABLE);
        }
    
        @Override
        public void becameAvailable() {
            setStatus(Status.API_AVAILABLE);
        }    
    };

    /**
     * Constructor.
     */
    private OSXConnector() {
    }

    /**
     * Gets the absolute path of Skype.
     * @return the absolute path of Skype.
     */
    public String getInstalledPath() {
        return "skype";
    }
    
    public boolean isRunning() throws ConnectorException {
        return SkypeFramework.isRunning();
    }

    /**
     * Initializes this connector.
     */
    protected void initialize() {
        SkypeFramework.init(getApplicationName());
        SkypeFramework.addSkypeFrameworkListener(listener);
    }

    /**
     * Connects to Skype client.
     * @param timeout the maximum time in milliseconds to connect.
     * @return Status the status after connecting.
     * @throws ConnectorException when connection can not be established.
     */
    protected Status connect(int timeout) throws ConnectorException {
        if (!SkypeFramework.isRunning()) {
            setStatus(Status.NOT_RUNNING);
            return getStatus();
        }
        if (!SkypeFramework.isAvailable()) {
            setStatus(Status.NOT_AVAILABLE);
            return getStatus();
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            SkypeFrameworkListener listener = new AbstractSkypeFrameworkListener() {            
               public void attachResponse(int attachResponseCode) {
                   SkypeFramework.removeSkypeFrameworkListener(this);
                   switch (attachResponseCode) {
                       case 0:
                           setStatus(Status.REFUSED);
                           latch.countDown();
                           break;
                       case 1:
                           setStatus(Status.ATTACHED);
                           latch.countDown();
                           break;
                       default:
                           throw new IllegalStateException("not supported attachResponseCode");
                   }
               }            
            };
            setStatus(Status.PENDING_AUTHORIZATION);
            SkypeFramework.addSkypeFrameworkListener(listener);
            SkypeFramework.connect();
            latch.await(timeout, TimeUnit.MILLISECONDS);
            return getStatus();
        } catch(InterruptedException e) {
            throw new ConnectorException("Trying to connect was interrupted.", e);
        }
    }

    /**
     * Sends a command to the Skype client.
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        SkypeFramework.sendCommand(command);
    }

    /**
     * Cleans up the connector and the native library.
     */
    protected void disposeImpl() {
        SkypeFramework.removeSkypeFrameworkListener(listener);
        SkypeFramework.dispose();
    }
}
