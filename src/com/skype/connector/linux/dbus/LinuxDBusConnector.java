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

    private String profilePath;

	private final String pass;

	private final String user;

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
    	profilePath = System.getProperty("user.home") + File.separator + ".Skype" + File.separator + user;
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
            readAvatarToFile(profilePath, userId, split[5]);
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

    /**
     * The Skype files that make up the user database
     */
    private static final String[] DBBs = new String[]{/*"user256.dbb",*/ "user1024.dbb", "user4096.dbb", "user16384.dbb", "user32768.dbb", "user65536.dbb"};

    /**
     * JPG Magic markers
     */
    private static final byte[] JPG_START_MARKER = new byte[]{(byte) 0xFF, (byte) 0xD8};
    private static final byte[] JPG_END_MARKER = new byte[]{(byte) 0xFF, (byte) 0xD9};
    
    /**
     * Marker which (appears to) separate user entries in .dbb files
     */
    private static final byte[] L33L_MARKER = "l33l".getBytes();

    private void readAvatarToFile(final String installPath, final String userId, final String path) {
        try {
            for(int i = 0; i < DBBs.length; i++) {
                File file = new File(installPath + File.separator + DBBs[i]);
                if(file != null && file.exists()) {
                    // read dbb file into a byte[]
                    final DataInputStream stream = new DataInputStream(new FileInputStream(file));
                    final byte[] bytes = new byte[stream.available()];
                    stream.read(bytes);

                    int pos = 0;
                    while(pos != bytes.length - 1) {
                        int l33l1 = indexOf(bytes, L33L_MARKER, pos);
                        if(l33l1 == -1) {
                            break;
                        }
                        
                        int l33l2 = indexOf(bytes, L33L_MARKER, l33l1 + 1);
                        if(l33l2 == -1) { // end of file
                            l33l2 = bytes.length - 1;
                        }
                        
                        // is userid owner of current l33l block?
                        int user = indexOf(bytes, userId.getBytes(), l33l1, l33l2);
                        if(user != -1) { // current l33l block is user we are looking for
                            
                            int jpgStart = indexOf(bytes, JPG_START_MARKER, l33l1, l33l2);
                            if(jpgStart != -1) { // l33l block contains jpg image

                                int jpgEnd = indexOf(bytes, JPG_END_MARKER, jpgStart, l33l2);
                                if(jpgEnd != -1) { // might happen as well
                                    
                                    // slice off jpg from dbb
                                    byte[] bs = Arrays.copyOfRange(bytes, jpgStart, jpgEnd + 2);
                                    
                                    // write to temp file
                                    FileOutputStream fos = new FileOutputStream(path);
                                    fos.write(bs);
                                    fos.close();
                                    
                                    return;
                                }
                            } else {
                                break;
                            }
                        }
                        
                        // advance to the next l33l marker
                        pos = l33l2;
                    }
                }    
            }

            // write dummy file
            InputStream in = getClass().getResourceAsStream("/dummy.jpg");
            FileOutputStream out = new FileOutputStream(path);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param bytes Where to search in
     * @param key The key to search for
     * @param from Must be < bytes.length
     * @return The first occurrence of the given key in bytes
     */
    private int indexOf(byte[] bytes, byte[] key, int from, int to) {
        OUTER: for(int i = from; i < to; i++) {
            // try to match first byte
            if(bytes[i] == key[0]) {
                for(int j = 0; j < key.length; j++) {
                    if(key[j] != bytes[i + j]) {
                        continue OUTER;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    private int indexOf(byte[] bytes, byte[] key, int from) {
        return indexOf(bytes, key, from, bytes.length);
    }
}
