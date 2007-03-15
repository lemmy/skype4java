/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot <bart.almot@gmail.com> 
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
 * Bart Lamot - initial API and implementation
 ******************************************************************************/
package com.skype.connector.linux;

public class LinuxConnector extends com.skype.connector.Connector {
	
	private static final String CONNECTOR_STATUS_CHANGED = "ConnectorStatusChanged";

	private static LinuxConnector _instance = null;
	
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

	public static synchronized LinuxConnector getInstance() {
		if (_instance == null) {
			_instance = new LinuxConnector();
		}
		return _instance;
	}

	public void init() {
		init(getApplicationName());
	}

	protected void sendCommand(final String command) {
		sendSkypeMessage(command);
	}
	
	protected void disposeImpl() {
		setConnectedStatus(5);	
		disposeNative();
		_instance = null;
	}

	protected Status connectImpl(int timeout) {
		if (getStatus() == Status.PENDING_AUTHORIZATION) {
			return Status.ATTACHED;
		}
		return getStatus();
	}

	protected void initialize(int timeout) {
		init();
	}
	
	private native void init(String applicationName);
	
	public native void sendSkypeMessage(String message);

	public native void disposeNative();

	public static void receiveSkypeMessage(String message) {
		if (_instance.getStatus() != Status.ATTACHED) {
			setConnectedStatus(1);
		}
		_instance.fireMessageReceived(message);
	}

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
