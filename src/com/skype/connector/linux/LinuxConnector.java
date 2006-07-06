/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot <bart.almot@gmail.com> 
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Bart Lamot
 * Koji Hisano - initial API and implementation Kamil Sarelo -
 ******************************************************************************/
package com.skype.connector.linux;

public class LinuxConnector extends com.skype.connector.Connector {
	
	private static final String CONNECTOR_STATUS_CHANGED = "ConnectorStatusChanged";

	private static LinuxConnector _instance = null;
	
	/**
	 * Private constructor to keep this a singleton.
	 * Use getInstance to get an instance.
	 */
	private LinuxConnector(){
		try {
			System.loadLibrary("JSA");
		} catch (Exception e) {
			setStatus(Status.NOT_AVAILABLE);
                	fireMessageReceived(CONNECTOR_STATUS_CHANGED);
		}
                setStatus(Status.PENDING_AUTHORIZATION);
                fireMessageReceived(CONNECTOR_STATUS_CHANGED);

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
	 * abstract method overridden.
	 */
	protected Status connectImpl(int timeout) {
		if (getStatus() == Status.PENDING_AUTHORIZATION) {
			return Status.ATTACHED;
		}
		return getStatus();
	}

	/**
	 * overriden method to initialize.
	 */
	protected void initialize(int timeout) {
		init();
	}
	
	private native void init(String applicationName);
	
	public native void sendSkypeMessage(String message);

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
		_instance.fireMessageReceived(CONNECTOR_STATUS_CHANGED);
	}
}
