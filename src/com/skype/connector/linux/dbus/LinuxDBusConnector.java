/*******************************************************************************
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * Copyright (c) 2011 Markus Alexander Kuppe.
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
 ******************************************************************************/
package com.skype.connector.linux.dbus;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.skype.Skype;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;

/**
 * Implementation of the connector for Linux
 */
public final class LinuxDBusConnector extends Connector {

	private final String pass;
	private final String user;
	private final AvatarReader avatarReader;

	private SkypeFramework skypeFramework;

    private SkypeFrameworkListener listener = new SkypeFrameworkListener() {
        public void notificationReceived(String notificationString) {
            fireMessageReceived(notificationString);
        }
    };
    
    /**
     * Constructor.
     */
    public LinuxDBusConnector(final Skype skype, final String aUsername, final String aPassword) {
    	super(skype);
    	user = aUsername;
    	pass = aPassword;
    	avatarReader = new AvatarReader(user);
    }

    public boolean isRunning() throws ConnectorException {
        return skypeFramework.isRunning();
    }

    /**
     * Gets the absolute path of Skype.
     * 
     * @return the absolute path of Skype.
     */
    public String getInstalledPath() {
        File application = new File("/usr/bin/skype");
        if(application.exists()) {
            return application.getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * Initializes this connector.
     */
    protected void initializeImpl() throws ConnectorException {
    	skypeFramework = new SkypeFramework(user, pass);
    	skypeFramework.init();
    	skypeFramework.addSkypeFrameworkListener(listener);
    }

    /**
     * Connects to Skype client.
     * 
     * @param timeout the maximum time in milliseconds to connect.
     * @return Status the status after connecting.
     * @throws ConnectorException when connection can not be established.
     */
    protected Status connect(int timeout) throws ConnectorException {
        if(!skypeFramework.isRunning()) {
            setStatus(Status.NOT_RUNNING);
            return getStatus();
        }
        try {
            final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
            SkypeFrameworkListener initListener = new SkypeFrameworkListener() {
                public void notificationReceived(String notification) {
                    if("OK".equals(notification) || "CONNSTATUS OFFLINE".equals(notification) || "ERROR 68".equals(notification)) {
                        try {
                            queue.put(notification);
                        } catch(InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            };
            setStatus(Status.PENDING_AUTHORIZATION);
            skypeFramework.addSkypeFrameworkListener(initListener);
            skypeFramework.sendCommand("NAME " + getApplicationName());
            String result = queue.take();
            skypeFramework.removeSkypeFrameworkListener(initListener);
            if("OK".equals(result)) {
                setStatus(Status.ATTACHED);
            } else if("CONNSTATUS OFFLINE".equals(result)) {
                setStatus(Status.NOT_AVAILABLE);
            } else if("ERROR 68".equals(result)) {
                setStatus(Status.REFUSED);
            }
            return getStatus();
        } catch(InterruptedException e) {
            throw new ConnectorException("Trying to connect was interrupted.", e);
        }
    }

    /**
     * Sends a command to the Skype client.
     * 
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        // using dbus to receive a user avatar fails with a general syntax error, thus
        // read the avatar from the skype .dbb files directly
        if(command.toLowerCase().contains("avatar")) {
            final String[] split = command.split(" ");
            final String userId = split[2];
            final String path = split[5];
            avatarReader.readAvatarToFile(userId, path);
            skypeFramework.fireNotificationReceived("USER " + userId + " AVATAR 1 ");
        } else {
        	skypeFramework.sendCommand(command);
        }
    }

    /**
     * Cleans up the connector and the native library.
     */
    protected void disposeImpl() {
        skypeFramework.removeSkypeFrameworkListener(listener);
        skypeFramework.dispose();
    }
}
