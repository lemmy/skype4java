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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

import com.Skype.API;
import com.Skype.Client;
import com.skype.connector.ConnectorException;

public class SkypeFramework {
	private static final String SERVER_PATH = "/com/Skype";
	private static final String CLIENT_PATH = "/com/Skype/Client";
	
	private static API skypeDBus;
	private static DBusConnection conn;

	private static final List<SkypeFrameworkListener> listeners = new CopyOnWriteArrayList<SkypeFrameworkListener>();
	
	private static class SkypeDBusNotify implements Client  {
		public boolean isRemote() {
			return false;
		}

		public void Notify(String message) {
			fireNotificationReceived(message);
		}
	}

	static void init() throws ConnectorException {
	    try {
	        conn = DBusConnection.getConnection(DBusConnection.SESSION);
	        
	        // register client listener
	        conn.exportObject(CLIENT_PATH, new SkypeDBusNotify());
	        
	        // get server
	        skypeDBus = conn.getRemoteObject("com.Skype.API", SERVER_PATH, API.class);
	    } catch(Exception e) {
	        throw new ConnectorException(e.getMessage(), e);
	    }
	}

	static void addSkypeFrameworkListener(SkypeFrameworkListener listener) {
		listeners.add(listener);
	}

	static void removeSkypeFrameworkListener(SkypeFrameworkListener listener) {
		listeners.remove(listener);
	}

	static boolean isRunning() {
		return true;
	}

	static void sendCommand(String command) {
	    System.err.println("=> sendcommand: " + command);
	    final String response = skypeDBus.Invoke(command);
		fireNotificationReceived(response);
	}

	static void fireNotificationReceived(String notificationString) {
		System.out
				.println("<= fireNotificationReceived: " + notificationString);
		for (SkypeFrameworkListener listener : listeners) {
			listener.notificationReceived(notificationString);
		}
	}

	static void dispose() {
		conn.unExportObject(CLIENT_PATH);
		conn.disconnect();
	}

}
