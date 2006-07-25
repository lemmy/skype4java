package com.skype.connector.osx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.skype.connector.Connector;

/**
 * 
 * @author Bart Lamot
 *
 */
public class OSXConnector extends Connector
    implements Runnable
{

	/** The singleton instance. */
    private static OSXConnector _instance = null;
    /** Thread synchronisation lock. */
    private static final Object lock = new Object();
    /** Don't reinit the native code over and over. */
    private boolean inited;

    /**
     * Constructor.
     * It loads the native library, if it can't be loaded the status is set to NOT_AVAILABLE.
     *
     */
    private OSXConnector()
    {
    	if (!checkLibraryInPath() || !checkInstalledFramework() )
    		getJarFile();
        try
        {
            System.loadLibrary("JSA");
        }
        catch(Throwable e)
        {
        	try {
        	System.load("/tmp/libJSA.jnilib");
        	} catch (Throwable e2) {
        		System.err.println("Could not load the library");
        		if (!checkLibraryInPath())
        			System.err.println("libJSA.jnilib is not in java.library.path");
        		if (!checkInstalledFramework())
        			System.err.println("Please install Skype.framework at /Library/Frameworks/Skype.framework");
        	   		//Sorry could not load.
        		setStatus(com.skype.connector.Connector.Status.NOT_AVAILABLE);
        		fireMessageReceived("ConnectorStatusChanged");
        	}
        }
    }
    
    /**
     * Checks if the library file can be found in the library path.
     * @return true if the file is found.
     */
    private boolean checkInstalledFramework() {
    	File frameworkLocation = new File("~/Library/Frameworks/Skype.framework");
    	return frameworkLocation.exists();
    }
    
    /**
     * Checks if the Skype.framework is properly installed.
     * @return true if the framework is found at the correct location.
     */
    private String getLibrarySearchPath() {
    	return System.getProperty("java.library.path")+":"+System.getProperty("user.dir")+":";
    }
    
    private boolean checkLibraryInPath() {
    	boolean libfilefound = false;
    	String libpath = getLibrarySearchPath();
    	File libfile = new File("");
    	StringTokenizer st = new StringTokenizer(libpath, libfile.pathSeparator);
    	while (st.hasMoreTokens() && !libfilefound) {
    		libfile = new File(st.nextToken()+"/libJSA.jnilib");
    		libfilefound = libfile.exists();
    	}
    	return libfilefound;
    }

    /**
     * Search for the jar file that contains this class in the classpath.
     * @return the file (directory) or NULL if not found.
     */
    private File getJarFile(){
    	boolean jarfilefound = false;
    	//System.out.println("CLASSPATH : " + System.getProperty("java.class.path"));
    	String classpath = System.getProperty("java.class.path");
    	File jarfile = null;
    	byte[] buf = new byte[1024];
    	String destinationname;
    	String jarFileName;
    	StringTokenizer st = new StringTokenizer(classpath, jarfile.pathSeparator);
    	while (st.hasMoreTokens() && !jarfilefound) {
    		jarFileName = st.nextToken();
    		jarfile = new File(jarFileName);
    		if (jarfile.exists() && jarfile.isFile()) {
    			 //Check the contents of this Jar file for the library or the Framework.
    			 FileInputStream fis = null;
				try {
					fis = new FileInputStream(jarFileName);
	    	        BufferedInputStream bis=new BufferedInputStream(fis);
	    	        ZipInputStream zis=new ZipInputStream(bis);
	    	        ZipEntry ze=null;
	    	        while ((ze=zis.getNextEntry())!=null) {
					     if (ze.getName().endsWith("libJSA.jnilib")){
					    	  destinationname = System.getProperty("java.io.tmpdir");
					     	if (!destinationname.endsWith(File.separator)) 
					     		destinationname = destinationname+File.separator;
					    	 //System.out.println("Found library in Jar file, writing it to: "+destinationname+"libJSA.jnilib");
					    	 int n;
					    	 FileOutputStream fileoutputstream;
				                fileoutputstream = new FileOutputStream(destinationname+"libJSA.jnilib");             
					                while ((n = zis.read(buf, 0, 1024)) > -1)
					                    fileoutputstream.write(buf, 0, n);
					                fileoutputstream.close(); 
					     }
					     if (ze.getName().endsWith("A/Skype")){
					    	  destinationname = System.getProperty("user.home");
					    	  if (!destinationname.endsWith(File.separator)) 
					     		destinationname = destinationname+File.separator;
					    	  //check for root
					    	  if (destinationname.endsWith("root/"))
					    		  destinationname = "/";
					     	 destinationname = destinationname+"Library/Frameworks/";
					    	 //System.out.println("Found Framework in Jar file, writing it to: "+destinationname+"Skype.framework");
					    	 //Make Framework directories
					    	 File frameworkDirectory = new File(destinationname+"Skype.framework/Versions/A/.tmp");
					    	 frameworkDirectory.mkdirs();
					    	 frameworkDirectory = new File(destinationname+"Skype.framework/Versions/Current/.tmp");
					    	 frameworkDirectory.mkdirs();
					    	 
					    	 int n = 0;
					    	 FileOutputStream fileoutputstream;
				                fileoutputstream = new FileOutputStream(destinationname+"Skype.framework/Versions/A/Skype");             
					                while ((n = zis.read(buf, 0, 1024)) > -1)
					                    fileoutputstream.write(buf, 0, n);
					                fileoutputstream.close();
					        n = 0;
					     	fileoutputstream = new FileOutputStream(destinationname+"Skype.framework/Versions/Current/Skype");             
				                while ((n = zis.read(buf, 0, 1024)) > -1)
				                    fileoutputstream.write(buf, 0, n);
				                fileoutputstream.close();
				         	
	    	        		n = 0;
	    	        		fileoutputstream = new FileOutputStream(destinationname+"Skype.framework/Skype");             
			                while ((n = zis.read(buf, 0, 1024)) > -1)
			                    fileoutputstream.write(buf, 0, n);
			                fileoutputstream.close();
					     }
			         		
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}    			
    	return null;
    }
    
    /**
     * Extract the libjni file from the jar file and put it into a folder.
     * @param jarfile	The jarfile to extract the lib file from.
     * @param destinationFolder The folder to put it in.
     */
    private void installLib(File jarfile, String destinationFolder){
    	
    }
    
    /**
     * Extract the Skype.framework from the jar file and put it into a folder.
     * @param jarfile The jar file to extract it out of.
     * @param destinationFolder the folder to put it in.
     */
    private void installFramework(File jarfile, String destinationFolder){
    	
    }
    
    /**
     * Return the singleton instance of this connector.
     * @return singleton instance.
     */
    public static OSXConnector getInstance()
    {
        //System.out.println("OSXConnector.getInstance()");
        if(_instance == null)
        	_instance = new OSXConnector();
        return _instance;
    }

    /**
     * Send a command to the Skype API.
     * @param command the command to send.
     */
    protected void sendCommand(String command)
    {
        //System.out.println((new StringBuilder()).append("OSXConnector.sendCommand(").append(command).append(") start").toString());
        sendSkypeMessage(command);
        //System.out.println((new StringBuilder()).append("OSXConnector.sendCommand(").append(command).append(") end").toString());
    }

    /**
     * Disconnect from the Skype API and clean up the native implementation.
     */
    protected void disposeImpl()
    {
        //System.out.println("OSXConnector.disposeImpl() start");
        setConnectedStatus(5);
        disposeNative();
        _instance = null;
        //System.out.println("OSXConnector.disposeImpl() end");
    }

    /**
     * Wait for the connection to get initialized.
     * @param timeout Wait for this amout of millisecs to initialize.
     */
    protected com.skype.connector.Connector.Status connectImpl(int timeout)
    {
        //System.out.println((new StringBuilder()).append("OSXConnector.connectImpl(").append(timeout).append(") start").toString());
        if (getStatus() == Status.PENDING_AUTHORIZATION) {
        	synchronized(lock)	
        	{
        		try
        		{
        			lock.wait(timeout);
        		}
        		catch(InterruptedException e)
        		{
        			e.printStackTrace();
        		}
        	}
        }
        //System.out.println((new StringBuilder()).append("OSXConnector.connectImpl(").append(timeout).append(") end").toString());
        return getStatus();
    }

    /**
     * Initialize the connection to Skype API.
     * @param timeout Wait for this amout of millisecs to initialize.
     */
    protected void initialize(int timeout)
    {
        //System.out.println((new StringBuilder()).append("OSXConnector.initialize(").append(timeout).append(") start ***************").toString());
        if(!inited)
        {
            inited = true;
            (new Thread(this)).start();
            setStatus(com.skype.connector.Connector.Status.PENDING_AUTHORIZATION);
            fireMessageReceived("ConnectorStatusChanged");
            setDebugPrinting(false);
            init(getApplicationName());
        }
        synchronized(lock)
        {
            try
            {
                lock.wait(timeout);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        //System.out.println((new StringBuilder()).append("OSXConnector.initialize(").append(timeout).append(") end ***************").toString());
    }

    /**
     * This method gets called by the native library when a message from Skype is received.
     * @param message The received message.
     */
    public static synchronized void receiveSkypeMessage(final String message)
    {
        //System.out.println((new StringBuilder()).append("OSXConnector.receiveSkypeMessage(").append(message).append(") start").toString());
        
        new Thread() { public void run() {
        	if(_instance.getStatus() != com.skype.connector.Connector.Status.ATTACHED)
            {
                setConnectedStatus(1);
                synchronized(lock)
                {
                    lock.notifyAll();
                }
            }
        	_instance.fireMessageReceived(message);	
        }}.start();
        
        //System.out.println((new StringBuilder()).append("OSXConnector.receiveSkypeMessage(").append(message).append(") end").toString());
    }

    /**
     * This method gets called from the native library when the status is changed by Skype API.
     * @param status the new status.
     */
    public static void setConnectedStatus(int status)
    {
        //System.out.println((new StringBuilder()).append("OSXConnector.setConnectedStatus(").append(status).append(") start").toString());
        synchronized(lock)
        {
            lock.notifyAll();
        }
        switch(status)
        {
        case 0: // '\0'
            _instance.setStatus(com.skype.connector.Connector.Status.PENDING_AUTHORIZATION);
            break;

        case 1: // '\001'
            _instance.setStatus(com.skype.connector.Connector.Status.ATTACHED);
            break;

        case 2: // '\002'
            _instance.setStatus(com.skype.connector.Connector.Status.REFUSED);
            break;

        case 3: // '\003'
            _instance.setStatus(com.skype.connector.Connector.Status.NOT_AVAILABLE);
            break;

        case 4: // '\004'
            _instance.setStatus(com.skype.connector.Connector.Status.API_AVAILABLE);
            break;

        case 5: // '\005'
            _instance.setStatus(com.skype.connector.Connector.Status.NOT_RUNNING);
            break;

        default:
            _instance.setStatus(com.skype.connector.Connector.Status.NOT_RUNNING);
            break;
        }
        _instance.fireMessageReceived("ConnectorStatusChanged");
        //System.out.println((new StringBuilder()).append("OSXConnector.setConnectedStatus(").append(status).append(") end").toString());
    }

    /**
     * OS X needs a native event loop to run, this starts it.
     */
    public void run()
    {
        startEventLoop();
    }
    
    private synchronized native void init(String s);

    private native void startEventLoop();

    private native void sendSkypeMessage(String s);

    private native void disposeNative();

    private native void setDebugPrinting(boolean flag);

}
