/*******************************************************************************
 * Copyright (c) 2006-2007 Bart Lamot <bart.almot@gmail.com> 
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
 * Bart Lamot - initial API and implementation
 ******************************************************************************/
package com.skype.connector.linux;

import java.io.File;

import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorUtils;
/**
 * Implementation of the Linux connector class.
 * Only works for DBus 0.22 not newer DBus or X11 messaging.
 */
public final class LinuxConnector extends com.skype.connector.Connector {
    /** Filename of the DLL. */
    private static final String LIBFILENAME = "libJSA.so";

    /** Singleton instance of this connector. */
	private static LinuxConnector _instance = null;
	
	/**
	 * Private constructor to keep this a singleton.
	 * Use getInstance to get an instance.
	 */
	private LinuxConnector(){
		try {
			System.loadLibrary("JSA");
		} catch (Throwable e) {
			try {
				if (!ConnectorUtils.checkLibraryInPath(LIBFILENAME)) {
		    		ConnectorUtils.extractFromJarToTemp(LIBFILENAME);   
		    		
		    		System.load(System.getProperty("java.io.tmpdir")+File.separatorChar+LIBFILENAME);
				}
			} catch (Exception e2) {
				setStatus(Status.NOT_AVAILABLE);
                return;
			}
		}   
		setStatus(Status.PENDING_AUTHORIZATION);
	}

	/**
	 * Get the singleton instance of this Connector.
	 * @return the singleton instance.
	 */
	public static synchronized LinuxConnector getInstance() {
		if (_instance == null) {
			_instance = new LinuxConnector();
		}
		return _instance;
	}

	/**
	 * Initialize the native library and connection with DBus.
	 *
	 */
	public void init() {
		init(getApplicationName());
	}

	/**
	 * Send a command to the Skype client using the native DBus code.
	 * @param command The command to send.
	 */
	protected void sendCommand(final String command) {
		sendSkypeMessage(command);
	}
	
	/**
	 * Dispose the native DBus connection.
	 */
	protected void disposeImpl() {
		setConnectedStatus(5);	
		disposeNative();
		_instance = null;
	}

	/**
	 * send the application name to the Skype client.
	 * @param newApplicationName the new application name.
	 * @throws ConnectorException when the connection with the Skype client has gone bad.
	 */
    protected void sendApplicationName(String newApplicationName) throws ConnectorException {
        execute("NAME " + newApplicationName, new String[] { "OK"  }, false);
    }

	/**
	 * abstract method overridden.
	 * @param timeout the maximum time to use in milliseconds to connect.
	 * @return The status after connecting.
	 */
	protected Status connect(int timeout) {
		if (getStatus() == Status.PENDING_AUTHORIZATION) {
			return Status.ATTACHED;
		}
		return getStatus();
	}

	/**
	 * overriden method to initialize.
	 * @param timeout the maximum time in milliseconds to initialize.
	 */
	protected void initialize() {
		init();
	}
	
	/**
	 * Native init method.
	 * @param applicationName Applicationname to set.
	 */
	private native void init(String applicationName);
	
	/**
	 * Native sendSkypeMessage method.
	 * @param message The message to send.
	 */
	public native void sendSkypeMessage(String message);

	/**
	 * Native dispose method.
	 * Cleans up and disconnects native library.
	 */
	public native void disposeNative();

	/**
	 * This method is used for callback by JNI code.
	 * When a message is received.
	 * @param message the received message.
	 */
	public static void receiveSkypeMessage(String message) {
		if (_instance.getStatus() != Status.ATTACHED) {
			setConnectedStatus(1);
		}
		_instance.fireMessageReceived(message);
	}

	/**
	 * This method is used for callback by JNI code to set the Status.
	 * @param status status to be set.
	 */
	public static void setConnectedStatus(int status) {
		switch(status) {
			case 0:	_instance.setStatus(Status.PENDING_AUTHORIZATION);break;
			case 1:	_instance.setStatus(Status.ATTACHED);break;
			case 2:	_instance.setStatus(Status.REFUSED);break;
			case 3:	_instance.setStatus(Status.NOT_AVAILABLE);break;	
			case 4:	_instance.setStatus(Status.API_AVAILABLE);break;
			case 5:	_instance.setStatus(Status.NOT_RUNNING);break;
			default:	_instance.setStatus(Status.NOT_RUNNING);break;
		}
	}
}
