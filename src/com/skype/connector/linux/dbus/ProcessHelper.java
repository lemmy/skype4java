/*
 * Date: Apr 3, 2011
 *
 * Copyright (C) 2011 Markus Alexander Kuppe. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skype.connector.linux.dbus;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessHelper {
    
    private static ProcessHelper INSTANCE;
    
    private final Map<String, Integer> usernameToPid = new HashMap<String, Integer>();
    private final Map<Integer, List<ProcessListener>> pidToListener = new HashMap<Integer, List<ProcessListener>>();

    public static synchronized ProcessHelper getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ProcessHelper();
        }
        return INSTANCE;
    }
    
    private ProcessHelper() {
    	// singleton
    }

    private int getPID(Process process) {
        if(process.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                return f.getInt(process);
            } catch(SecurityException e) {
                e.printStackTrace();
            } catch(NoSuchFieldException e) {
                e.printStackTrace();
            } catch(IllegalArgumentException e) {
                e.printStackTrace();
            } catch(IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public synchronized int getSkypeProcess(final ProcessListener listener, final String user, final String pass) throws IOException {
        Integer pid = usernameToPid.get(user);
        if (pid != null) {
        	List<ProcessListener> list = pidToListener.get(pid);
        	list.add(listener);
            return pid;
        } else {
            // build command line
            final List<String> cmds = new ArrayList<String>();
            cmds.add("/usr/bin/skype");
            cmds.add("--pipelogin");
            
            // spawn process
            final Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec(cmds.toArray(new String[cmds.size()]));
            
            // pipe user and pass to process
            final OutputStream outputStream = process.getOutputStream();
            outputStream.write((user + " " + pass).getBytes());
            outputStream.close();
            
            // store for subsequent use
            pid = getPID(process);
            usernameToPid.put(user, pid);
           
            // if process gets killed externally, remove from holder
            List<ProcessListener> listeners = new ArrayList<ProcessListener>();
            listeners.add(listener);
			pidToListener.put(pid, listeners);
            final Thread monitor = new Thread(new Runnable() {
                public void run() {
                    try {
                        process.waitFor();
                        int p = getPID(process);
						Integer remove = usernameToPid.remove(user);
						assert p == remove;
                    	List<ProcessListener> list = pidToListener.get(p);
                    	for (ProcessListener processListener : list) {
							processListener.processTerminated();
						}
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Skype process watcher for pid " + pid);
            monitor.start();
            
            // kill skype process on VM shutdown
            //TODO make configurable
            runtime.addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    if(process != null) {
                        process.destroy();
                    }
                }
            }));
            
            return pid;
        }
    }

	public boolean removeProcessListener(int aPid, ProcessListener aProcessListener) {
		List<ProcessListener> list = pidToListener.get(aPid);
		if(list != null) {
			return list.remove(aProcessListener);
		}
		return false;
	}
}
