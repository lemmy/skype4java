package com.skype.connector.osx;

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
        inited = false;
        try
        {
            System.loadLibrary("JSA");
        }
        catch(Exception e)
        {
            setStatus(com.skype.connector.Connector.Status.NOT_AVAILABLE);
            fireMessageReceived("ConnectorStatusChanged");
        }
    }

    /**
     * Return the singleton instance of this connector.
     * @return singleton instance.
     */
    public static synchronized OSXConnector getInstance()
    {
        System.out.println("OSXConnector.getInstance()");
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
        System.out.println((new StringBuilder()).append("OSXConnector.sendCommand(").append(command).append(") start").toString());
        sendSkypeMessage(command);
        System.out.println((new StringBuilder()).append("OSXConnector.sendCommand(").append(command).append(") end").toString());
    }

    /**
     * Disconnect from the Skype API and clean up the native implementation.
     */
    protected void disposeImpl()
    {
        System.out.println("OSXConnector.disposeImpl() start");
        setConnectedStatus(5);
        disposeNative();
        _instance = null;
        System.out.println("OSXConnector.disposeImpl() end");
    }

    /**
     * Wait for the connection to get initialized.
     * @param timeout Wait for this amout of millisecs to initialize.
     */
    protected com.skype.connector.Connector.Status connectImpl(int timeout)
    {
        System.out.println((new StringBuilder()).append("OSXConnector.connectImpl(").append(timeout).append(") start").toString());
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
        System.out.println((new StringBuilder()).append("OSXConnector.connectImpl(").append(timeout).append(") end").toString());
        return getStatus();
    }

    /**
     * Initialize the connection to Skype API.
     * @param timeout Wait for this amout of millisecs to initialize.
     */
    protected void initialize(int timeout)
    {
        System.out.println((new StringBuilder()).append("OSXConnector.initialize(").append(timeout).append(") start ***************").toString());
        if(!inited)
        {
            inited = true;
            (new Thread(this)).start();
            setStatus(com.skype.connector.Connector.Status.PENDING_AUTHORIZATION);
            fireMessageReceived("ConnectorStatusChanged");
            setDebugPrinting(true);
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
        System.out.println((new StringBuilder()).append("OSXConnector.initialize(").append(timeout).append(") end ***************").toString());
    }

    /**
     * This method gets called by the native library when a message from Skype is received.
     * @param message The received message.
     */
    public static void receiveSkypeMessage(String message)
    {
        System.out.println((new StringBuilder()).append("OSXConnector.receiveSkypeMessage(").append(message).append(") start").toString());
        if(_instance.getStatus() != com.skype.connector.Connector.Status.ATTACHED)
        {
            System.out.println((new StringBuilder()).append("OSXConnector.receiveSkypeMessage(").append(message).append(") Status =").append(_instance.getStatus()).toString());
            setConnectedStatus(1);
            synchronized(lock)
            {
                lock.notifyAll();
            }
        }
        _instance.fireMessageReceived(message);
        System.out.println((new StringBuilder()).append("OSXConnector.receiveSkypeMessage(").append(message).append(") end").toString());
    }

    /**
     * This method gets called from the native library when the status is changed by Skype API.
     * @param status the new status.
     */
    public static void setConnectedStatus(int status)
    {
        System.out.println((new StringBuilder()).append("OSXConnector.setConnectedStatus(").append(status).append(") start").toString());
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
        System.out.println((new StringBuilder()).append("OSXConnector.setConnectedStatus(").append(status).append(") end").toString());
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
