/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot
 *
 * Contributors:
 *     Bart Lamot - initial API and implementation
 *******************************************************************************/
package jp.sf.skype.connector.osx;

import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;
import jp.sf.skype.connector.ConnectorMessageReceivedListener;
import jp.sf.skype.connector.Connector.Status;

public final class OSXConnector extends Connector {
	  /**
     * The singleton instance of the OSXConnector class.
     */
	private static OSXConnector instance = null;

	 /**
     * The attached response type (value is 0).
     * <p>
     * This response is sent when the client is attached.
     * </p>
     */
    private static final int ATTACH_SUCCESS = 0;
    /**
     * The pending authorization response type (value is 1).
     * <p>
     * This response is sent when Skype acknowledges the connection request and is waiting for user confirmation.
     * The client is not yet attached and must wait for the {@ses #ATTACH_SUCCESS} message.
     * </p>
     */
    private static final int ATTACH_PENDING_AUTHORIZATION = 1;
    /**
     * The refused response type (value is 2).
     * <p>
     * This response is sent when the user has explicitly denied the access of the client.
     * </p>
     */
    private static final int ATTACH_REFUSED = 2;
    /**
     * The not available response type (value is 3).
     * <p>
     * This response is sent when the API is not available at the moment because no user is currently logged in.
     * The client must wait for a {@see #ATTACH_API_AVAILABLE} broadcast before attempting to connect again.
     * </p>
     */
    private static final int ATTACH_NOT_AVAILABLE = 3;
    /**
     * The available response type (value is 0x8001).
     * <p>
     * This response is sent when the API becomes available.
     */
    private static final int ATTACH_API_AVAILABLE = 4;
    
    private static final String CONNECTOR_STATUS_CHANGED_MESSAGE = "ConnectorStatusChanged";
	
    /**
     * Block the usage of the constructor because this is a singleton.
     * Use getInstance();
     *
     */
    private OSXConnector() {
    	
    }
	
	/**
     * Gets the singleton instance of the WindowsConnector class.
     * 
     * @return the singleton instance of the WindowsConnector class
     */
    public static synchronized OSXConnector getInstance() {
        if (instance == null) {
            instance = new OSXConnector();
        }
        return instance;
    }
    
    @Override
    protected void disposeImpl() {
    	try {
			nativeDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    protected void sendCommand(final String command) {
    	System.out.println("OSXConnector.sendCommand("+command+")");
		//Check if a connection with the API is made.
		if (instance.getStatus() == Status.ATTACHED ) {
			nativeSendMessage(command.trim()+" ");
		} else {
			//There is no connection with the API; we cannot send.
		}
    }
    
	   @Override
    public String getInstalledPath() {
       return null;
    }


    @Override
    protected void initialize(int timeout) throws ConnectorException {
        final Object object = new Object();
        synchronized (object) {
            try {
        		instance = this;
        		//	 Load JNI library
        		  System.loadLibrary("JSA");
        		try
        		{
        			System.loadLibrary( "JSA" );
        			nativeInit();			
        		}
        		catch( UnsatisfiedLinkError x )
        		{
        			x.printStackTrace();
        		}

/*
            	long start = System.currentTimeMillis();
                object.wait(timeout);
                if (timeout <= System.currentTimeMillis() - start) {
                    throw new ConnectorException("The OSX  connector couldn't be initialized by timeout.");
                }
                */
            } catch (Exception e) {
                throw new ConnectorException("The OSX connector initialization was interrupted.", e);
            }
        }
    }

    @Override
    protected Status connectImpl(int timeout) throws ConnectorException {
    	   final Object object = new Object();
           ConnectorMessageReceivedListener listener = new ConnectorMessageReceivedListener() {
               public void messageReceived(String message) {
                   if (message.equals(CONNECTOR_STATUS_CHANGED_MESSAGE)) {
                       synchronized (object) {
                           object.notify();
                       }
                   }
               }
           };
           try {
               addConnectorMessageReceivedListener(listener, false);
           } catch (ConnectorException e) {
               throw new InternalError("The listener couldn't be added."); // The flow must not reach here.
           }
    	return Status.ATTACHED;
	}

 

    /**
     * Main, only used to do a quick test in XCode.
     * @param args None used
     */
    public static void main (String args[]) {
        // insert code here...
        System.out.println("Started JNIWrapper");
        OSXConnector newjni = new OSXConnector();
        try {
        	newjni.initialize(30000);
        	newjni.waitforresponse();
        	newjni.disposeImpl();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        System.out.println("Finished");
    }
	
	/**
	 * Used by XCode test 
	 *
	 */
	void waitforresponse() {
	    System.out.print("Waiting...");
		for (int i=0; i < 6000 ; i++) {
		   try {
		     Thread.yield();
			 Thread.sleep(10);
		   } catch (Exception e) {
		     
			}
		   System.out.print(".");
		}
		System.out.println("done waiting");
	}
	
	private synchronized static native int nativeSendMessage( String message ) throws UnsatisfiedLinkError;
	private synchronized static native int nativeDestroy() throws UnsatisfiedLinkError;
	private synchronized static native int nativeInit() throws UnsatisfiedLinkError;
	
	/**
	 * This method is called by the native lib when a skype message is received.
	 * @param skypeID the ID of the Skype instance
	 * @param message the message received
	 */
	public static void onCallback(final int skypeID,final String message) {
		System.out.println("OSXConnector.onCallBack("+message+")");
		instance.fireMessageReceived(message);
	}

	/**
	 * Called by the native lib when a skype instance is found.
	 *
	 */
	public static void onSkypeAPIStatus(int status){
		System.out.println("OSXConnector.onSkypeAPIStatus("+status+")");
	       switch (status) {
           case ATTACH_PENDING_AUTHORIZATION:
               instance.setStatus(Status.PENDING_AUTHORIZATION);
               break;
           case ATTACH_SUCCESS:
               instance.setStatus(Status.ATTACHED);
               break;
           case ATTACH_REFUSED:
        	   instance.setStatus(Status.REFUSED);
               break;
           case ATTACH_NOT_AVAILABLE:
        	   instance.setStatus(Status.NOT_AVAILABLE);
               break;
           case ATTACH_API_AVAILABLE:
        	   instance.setStatus(Status.API_AVAILABLE);
               break;
       }
	   instance.fireMessageReceived(CONNECTOR_STATUS_CHANGED_MESSAGE);
	   System.out.println("OSXConnector.onSkypeAPIStatus status set to:"+instance.getStatus());
	   //currentAttachAPIStatus = status;
		//noresponse = false;
	}

}
