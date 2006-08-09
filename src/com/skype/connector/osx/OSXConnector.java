package com.skype.connector.osx;

import java.io.File;
import com.skype.connector.Connector;
import com.skype.connector.Utils;

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
    /** Name of the library. */
    private static final String LIBNAME = "JSA";
    /** Filename of the OS X jnilib. */
    private static final String LIBFILENAME = "libJSA.jnilib";

    /**
     * Constructor.
     * It loads the native library, if it can't be loaded the status is set to NOT_AVAILABLE.
     *
     */
    private OSXConnector()
    {
    	if (!Utils.checkLibraryInPath(LIBFILENAME) || !checkInstalledFramework() ) {
    		Utils.extractFromJarToTemp(LIBFILENAME);
    		installFramework();
    	}
        try
        {
            System.loadLibrary(LIBNAME);
        }
        catch(Throwable e)
        {
        	try {
        	System.load(System.getProperty("java.io.tmpdir")+File.separatorChar+LIBFILENAME);
        	} catch (Throwable e2) {
        		System.err.println("Could not load the library");
        		if (!Utils.checkLibraryInPath(LIBFILENAME))
        			System.err.println(LIBFILENAME+" is not in java.library.path");
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
    	File frameworkLocationHome = new File("~/Library/Frameworks/Skype.framework");
    	File frameworkLocationSystem = new File("/Library/Frameworks/Skype.framework");
    	if (frameworkLocationHome.exists() || frameworkLocationSystem.exists()) 
    		return true;
    	return false;
    }

    /**
     * Install the Skype.framework from the jarfile.
     * create directories and extract framework files for jar.
     */
    private void installFramework(){
    	//First check if Framework is in jarfile. Lets not create directories if nothing can be found.
    	if (Utils.isInJar("A/Skype")) {
    		String destinationname;
    		destinationname = System.getProperty("user.home");
    		if (!destinationname.endsWith(File.separator)) 
				destinationname = destinationname+File.separator;
			//check for root, root doesn't have a Users home directory.
			if (destinationname.endsWith("root/"))
				destinationname = "/";
		 	destinationname = destinationname+"Library/Frameworks/";
		 	//Make Framework directories
			File frameworkDirectory = new File(destinationname+"Skype.framework/Versions/A/.tmp");
			frameworkDirectory.mkdirs();
			frameworkDirectory = new File(destinationname+"Skype.framework/Versions/Current/.tmp");
			frameworkDirectory.mkdirs();
			Utils.extractFromJar("A/Skype", "Skype", destinationname+"Skype.framework/Versions/A");
			Utils.extractFromJar("A/Skype", "Skype", destinationname+"Skype.framework/Versions/Current");
			Utils.extractFromJar("A/Skype", "Skype", destinationname+"Skype.framework");
    	}
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
    protected com.skype.connector.Connector.Status connect(int timeout)
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
