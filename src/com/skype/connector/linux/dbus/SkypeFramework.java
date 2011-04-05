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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.exceptions.DBusException;

import com.Skype.API;
import com.Skype.Client;
import com.skype.connector.ConnectorException;

public class SkypeFramework {
    private static final boolean DEBUG = Boolean.getBoolean("com.skype.connector.linux.dbus.SkypeFramework.debug");

    private static final String SERVER_PATH = "/com/Skype";
	private static final String CLIENT_PATH = "/com/Skype/Client";
	
	private class SkypeDBusNotify implements Client  {
		
        private SkypeFramework fw;

		public SkypeDBusNotify(SkypeFramework fw) {
			this.fw = fw;
		}
		
		public boolean isRemote() {
			return false;
		}

		public void Notify(String message) {
		    if(DEBUG) {
		        System.out.println("<= fireListenerNotify: " + message);
		    }
		    fw.fireNotificationReceived(message);
		}
	}

    private final List<SkypeFrameworkListener> listeners = new CopyOnWriteArrayList<SkypeFrameworkListener>();
	private final String pass;
	private final String user;

	private API skypeDBus;
	private DBusConnection conn;
    private String source;

    private ProcessHelper processHelper;

	private ProcessListener processListener;
	private int pid;

	public SkypeFramework(String aUsername, String aPassword) {
		this.user = aUsername;
		this.pass = aPassword;
		this.processHelper = ProcessHelper.getInstance();
	}

    void init(ProcessListener aProcessListener) throws ConnectorException {
        try {
        	this.processListener = aProcessListener;
            pid = processHelper.getSkypeProcess(aProcessListener, user, pass);
            
            conn = DBusConnection.getConnection(DBusConnection.SESSION);
            source = getDBusAddressByPid(pid);
            
            // get handle for server
            skypeDBus = conn.getRemoteObject(source, SERVER_PATH, API.class);
            
            // add generic client listener for the given skype name
            conn.exportObject(CLIENT_PATH, source, new SkypeDBusNotify(this));
        } catch (DBusException e) {
            throw new ConnectorException(e.getMessage(), e);
        } catch(IOException e) {
            throw new ConnectorException(e.getMessage(), e);
        }
	}

    private String getDBusAddressByPid(int pid) throws DBusException {
        final DBus dbus = conn.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus.class);

        int max = 0;
        String source = "";
        // find skype DBus name by process id
        while ("".equals(source)) {
        	if(max++ == 10) {
        		throw new ConnectorException("No Skype instance with pid " + pid + " found attached to DBus t");
        	}
            // might take some time for skype to register with dbus
            try {
                Thread.sleep(5000); 
            } catch(InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
            }

            final String[] names = dbus.ListNames();
            Arrays.sort(names); // increase likelihood of finding the skype process fast
        	for(int i = names.length - 1; i >= 0; i--) {
        		final String name = names[i];
        		if(name.startsWith(":")) {
        			final UInt32 getConnectionUnixProcessID = dbus.GetConnectionUnixProcessID(name);
        			if(pid == getConnectionUnixProcessID.intValue()) {
        			    source = name;
        				break;
        			}
        		}
        	}
        }
        return source;
    }
    
    void addSkypeFrameworkListener(SkypeFrameworkListener listener) {
		listeners.add(listener);
	}

	void removeSkypeFrameworkListener(SkypeFrameworkListener listener) {
		listeners.remove(listener);
	}

	boolean isRunning() {
		return conn != null;
	}

	void sendCommand(String command) {
	    if(DEBUG) {
	        System.err.println("=> sendcommand: " + command);
	    }
	    final String response = skypeDBus.Invoke(command);
		fireNotificationReceived(response);
	}
	
	String sendCommndWithResponse(String command) {
	    return skypeDBus.Invoke(command);
	}

	void fireNotificationReceived(String notificationString) {
	    if(DEBUG) {
	        System.out
	        .println("<= fireNotificationReceived: " + notificationString);
	    }
		for (SkypeFrameworkListener listener : listeners) {
			listener.notificationReceived(notificationString);
		}
	}

	void dispose() {
		conn.unExportObject(CLIENT_PATH, source);
		conn.disconnect();
		conn = null; 
		processHelper.removeProcessListener(pid, processListener);
	}
}
