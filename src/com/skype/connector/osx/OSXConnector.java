/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot
 * 
 * Contributors: Bart Lamot - initial API and implementation
 ******************************************************************************/
package com.skype.connector.osx;

import com.skype.connector.Connector;

public class OSXConnector extends Connector implements Runnable{

	private static final String CONNECTOR_STATUS_CHANGED = "ConnectorStatusChanged";

	private static OSXConnector _instance = null;

	private static Object lock = new Object();
	
	/**
	 * Private constructor to keep this a singleton.
	 * Use getInstance to get an instance.
	 */
	private OSXConnector(){
		System.out.println("OSXConnector.OSXConnector()");
		new Thread(this).start();
	}

	/**
	 * Get the singleton instance of this Connector.
	 * @return the singleton instance.
	 */
	public static synchronized OSXConnector getInstance() {
		System.out.println("OSXConnector.getInstance()");
		if (_instance == null) {
			_instance = new OSXConnector();
		}
		return _instance;
	}

	/**
	 * Initialize the native library and connection with DBus.
	 *
	 */
	public void init() {
		System.out.println("OSXConnector.init() start");
		init(getApplicationName());
		System.out.println("OSXConnector.init() end");
	}

	/**
	 * Send a command to the Skype client using the native DBus code.
	 */
	@Override
	protected void sendCommand(final String command) {
		System.out.println("OSXConnector.sendCommand("+command+") start");
		sendSkypeMessage(command);
		System.out.println("OSXConnector.sendCommand("+command+") end");
	}
	
	/**
	 * Dispose the native DBus connection.
	 */
	@Override
	protected void disposeImpl() {
		System.out.println("OSXConnector.disposeImpl() start");
		setConnectedStatus(5);	
		disposeNative();
		_instance = null;
		System.out.println("OSXConnector.disposeImpl() end");
	}

	/**
	 * abstract method overridden.
	 */
	@Override
	protected Status connectImpl(int timeout) {
		System.out.println("OSXConnector.connectImpl("+timeout+")");
		if (getStatus() == Status.PENDING_AUTHORIZATION){
				synchronized(lock) {
					try {
						lock.wait(timeout);
					} catch (InterruptedException e) {
					e.printStackTrace();
					}
				}
				System.out.println("OSXConnector.connectImpl("+timeout+") end 1 ->"+Status.ATTACHED);
				return Status.ATTACHED;
		}
		System.out.println("OSXConnector.connectImpl("+timeout+") end 2 ->"+getStatus());
		return getStatus();
	}

	/**
	 * overriden method to initialize.
	 */
	@Override
	protected void initialize(int timeout) {
		System.out.println("OSXConnector.initialize("+timeout+") start");
		try {
			System.loadLibrary("JSA");
		} catch (Exception e) {
			setStatus(Status.NOT_AVAILABLE);
			fireMessageReceived(CONNECTOR_STATUS_CHANGED);
		}
        setStatus(Status.PENDING_AUTHORIZATION);
        fireMessageReceived(CONNECTOR_STATUS_CHANGED);
        
        init();
		System.out.println("OSXConnector.initialize("+timeout+") end");
	}
	
	private native void init(String applicationName);
	
	private native void sendSkypeMessage(String message);

	private native void disposeNative();

	/**
	 * This method is used for callback by JNI code.
	 * When a message is received.
	 * @param message the received message.
	 */
	public static void receiveSkypeMessage(String message) {
		System.out.println("OSXConnector.receiveSkypeMessage("+message+") start");
		lock.notify();
		if (_instance.getStatus() != Status.ATTACHED) {
			setConnectedStatus(1);
		}
		_instance.fireMessageReceived(message);
		System.out.println("OSXConnector.receiveSkypeMessage("+message+") end");
	}

	/**
	 * This method is used for callback by JNI code to set the Status.
	 * @param status status to be set.
	 */
	public static void setConnectedStatus(int status) {
		System.out.println("OSXConnector.setConnectedStatus("+status+") start");
		lock.notify();
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
		System.out.println("OSXConnector.setConnectedStatus("+status+") end");
	}

	public void run() {
		
	}
	
}
