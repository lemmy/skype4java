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
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.UInt32;

import com.Skype.API;
import com.Skype.Client;
import com.skype.connector.ConnectorException;

public class SkypeFramework {
	private static final String SERVER_PATH = "/com/Skype";
	private static final String CLIENT_PATH = "/com/Skype/Client";
	
	private static API skypeDBus;
	private static DBusConnection conn;

	private static final List<SkypeFrameworkListener> listeners = new CopyOnWriteArrayList<SkypeFrameworkListener>();
	
	private class SkypeDBusNotify implements Client  {
		
		private SkypeFramework fw;

		public SkypeDBusNotify(SkypeFramework fw) {
			this.fw = fw;
		}
		
		public boolean isRemote() {
			return false;
		}

		public void Notify(String message) {
		    System.out.println("<= fireListenerNotify: " + message);
		    fw.fireNotificationReceived(message);
		}
	}

	private String pass;
	private String user;

	public SkypeFramework(String aUsername, String aPassword) {
		this.user = aUsername;
		this.pass = aPassword;
	}

	@SuppressWarnings("unused")
    void init() throws ConnectorException {
	    
        Process process = null;
        try {
//            SkypeDBusDaemon dBusDaemon = new SkypeDBusDaemon();
//        
//            String string = dBusDaemon.getBusAddress().toString();
//            System.out.println("export DBUS_SESSION_BUS_ADDRESS=\"" + string + "\" && qdbusviewer");
            String string = null;
            process = spawnSkypeProcess(string, user, pass);
            addShutdownHook(process);
	        int pid = getPID(process);
	        
//            File f = new File("/tmp/addressfile");
//            BufferedReader fr = new BufferedReader(new FileReader(f));
//            String string = fr.readLine();
//            String string = "tcp:host=localhost,port=43916,guid=6bbd386de469eb9811ab039ed7826c65";

	        if(string != null) {
	            conn = DBusConnection.getConnection(string);
	        } else {
                conn = DBusConnection.getConnection(DBusConnection.SESSION);
	        }
            final DBus dbus = conn.getRemoteObject("org.freedesktop.DBus", "/org/freedesktop/DBus", DBus.class);

            
            String source = "";
            // find skype DBus name by process id
            while ("".equals(source)) {
                // might take some time for skype to register with dbus
                Thread.sleep(5000); 

                final String[] names = dbus.ListNames();
                Arrays.sort(names); // increase likelihood of finding the skype process fast
            	for(int i = names.length - 1; i >= 0; i--) {
            		final String name = names[i];
            		if(name.startsWith(":")) {
            			UInt32 getConnectionUnixProcessID = dbus.GetConnectionUnixProcessID(name);
            			if(pid == getConnectionUnixProcessID.intValue()) {
            			    source  = name;
            				break;
            			}
            		}
            	}
            }

            // get handle for server
            skypeDBus = conn.getRemoteObject("com.Skype.API", SERVER_PATH, API.class);

            // add generic client listener for the given skype name
            // replaces: conn.exportObject(CLIENT_PATH, new SkypeDBusNotify());
            conn.exportObject(CLIENT_PATH, source, new SkypeDBusNotify(this));
	    } catch(Exception e) {
	        if(process != null) {
	            process.destroy();
	        }
	        throw new ConnectorException(e.getMessage(), e);
	    }
	}

	private void addShutdownHook(final Process process) {
	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if(process != null) {
                    process.destroy();
                }
            }
	    }));
    }

    private int getPID(Process process) {
	    if(process.getClass().getName().equals("java.lang.UNIXProcess")) {
	        try {
	            Field f = process.getClass().getDeclaredField("pid");
	            f.setAccessible(true);
	            return f.getInt(process);
	        } catch (SecurityException e) {
	            e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        }
	    }
	    return -1;
    }

    private Process spawnSkypeProcess(final String dbusAddress, final String user, final String pass) throws IOException {
        List<String> cmds = new ArrayList<String>();
//        cmds.add("/bin/bash");
//        cmds.add("-c");
//        cmds.add("/bin/echo " + user +  " " + pass + " | /usr/bin/skype --pipelogin");
        cmds.add("/usr/bin/skype");
        cmds.add("--pipelogin");
        
        Runtime runtime = Runtime.getRuntime();

        Process process;
        if(dbusAddress != null) {
            List<String> envs = new ArrayList<String>();
            envs.add("DBUS_SESSION_BUS_ADDRESS=" + dbusAddress);
            envs.add("USER=" + System.getProperty("user.name"));
            envs.add("HOME=" + System.getProperty("user.home"));
            envs.add("DISPLAY=" + System.getenv("DISPLAY"));
            process = runtime.exec(cmds.toArray(new String[cmds.size()]), envs.toArray(new String[envs.size()]));
        } else {
            process = runtime.exec(cmds.toArray(new String[cmds.size()]));
        }
        

        OutputStream outputStream = process.getOutputStream();
        outputStream.write((user + " " + pass).getBytes());
        outputStream.close();
        
        return process;
        
//        ProcessBuilder builder = new ProcessBuilder(cmds);
//        builder.redirectErrorStream(true);
//        return builder.start();

//        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
//        Executor executor = new DefaultExecutor();
//        
//        // kill run away process
//        ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
//        executor.setWatchdog(watchdog);
//
//        // build command line
//        CommandLine cl = new CommandLine("/usr/bin/skype");
//                cl.addArgument("--pipelogin");
//
//        // pipe user/pass to skype process
//        String text = user + " " + pass;
//        ByteArrayInputStream input =
//            new ByteArrayInputStream(text.getBytes("ISO-8859-1"));
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        executor.setStreamHandler(new PumpStreamHandler(output, null, input));
//
//        // get going
//        executor.execute(cl, resultHandler);
//        return null;
    }
    

    void addSkypeFrameworkListener(SkypeFrameworkListener listener) {
		listeners.add(listener);
	}

	void removeSkypeFrameworkListener(SkypeFrameworkListener listener) {
		listeners.remove(listener);
	}

	boolean isRunning() {
		return true;
	}

	void sendCommand(String command) {
	    System.err.println("=> sendcommand: " + command);
	    final String response = skypeDBus.Invoke(command);
		fireNotificationReceived(response);
	}
	
	String sendCommndWithResponse(String command) {
	    return skypeDBus.Invoke(command);
	}

	void fireNotificationReceived(String notificationString) {
		System.out
				.println("<= fireNotificationReceived: " + notificationString);
		for (SkypeFrameworkListener listener : listeners) {
			listener.notificationReceived(notificationString);
		}
	}

	void dispose() {
		conn.unExportObject(CLIENT_PATH);
		conn.disconnect();
	}
}
