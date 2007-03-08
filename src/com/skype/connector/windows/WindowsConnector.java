/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Kamil Sarelo - modified getInstalledPath() to support installing by an
 *                administrator account and added javadocs
 ******************************************************************************/
package com.skype.connector.windows;

import java.io.UnsupportedEncodingException;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.internal.win32.WNDCLASS;
import org.eclipse.swt.widgets.Display;

import com.skype.connector.AbstractConnectorListener;
import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorListener;
import com.skype.connector.ConnectorStatusEvent;

/**
 * Implementation of the Windows connector based on the SWT libraries.
 * Please, use Win32Connector if SWT is not an option for you.
 */
public final class WindowsConnector extends Connector {
    /**
     * The singleton instance of the WindowsConnector class.
     */
    private static class Instance {
        static WindowsConnector instance = new WindowsConnector();
    }

    /**
     * Gets the singleton instance of the WindowsConnector class.
     * 
     * @return the singleton instance of the WindowsConnector class
     */
    public static WindowsConnector getInstance() {
        // Using 'Initialization On Demand Holder' Pattern
        return Instance.instance;
    }

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
     * This response is sent when Skype acknowledges the connection request and
     * is waiting for user confirmation. The client is not yet attached and must
     * wait for the {@ses #ATTACH_SUCCESS} message.
     * </p>
     */
    private static final int ATTACH_PENDING_AUTHORIZATION = 1;

    /**
     * The refused response type (value is 2).
     * <p>
     * This response is sent when the user has explicitly denied the access of
     * the client.
     * </p>
     */
    private static final int ATTACH_REFUSED = 2;

    /**
     * The not available response type (value is 3).
     * <p>
     * This response is sent when the API is not available at the moment because
     * no user is currently logged in. The client must wait for a
     * {@see #ATTACH_API_AVAILABLE} broadcast before attempting to connect
     * again.
     * </p>
     */
    private static final int ATTACH_NOT_AVAILABLE = 3;

    /**
     * The available response type (value is 0x8001).
     * <p>
     * This response is sent when the API becomes available.
     */
    private static final int ATTACH_API_AVAILABLE = 0x8001;

    /**
     * The window handle indicating all top-level windows in the system.
     * 
     * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/winui/winui/windowsuserinterface/windowing/messagesandmessagequeues/messagesandmessagequeuesreference/messagesandmessagequeuesfunctions/sendmessage.asp">MSDN Library</a>
     */
    private static final int HWND_BROADCAST = 0xffff;

    /**
     * The window message type to pass data to another application.
     * 
     * @see <a href="http://search.msdn.microsoft.com/search/Redirect.aspx?title=WM_COPYDATA+Message&url=http://msdn.microsoft.com/library/en-us/winui/winui/windowsuserinterface/dataexchange/datacopy/datacopyreference/datacopymessages/wm_copydata.asp">MSDN Library</a>
     */
    private static final int WM_COPYDATA = 0x004a;

    /**
     * The window message type of the response for initiating communication from Skype.
     * 
     * @see #DISCOVER_MESSAGE_ID
     */
    private static final int ATTACH_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIAttach", true));

    /**
     * The window message type of the request to initiate communication with Skype.
     * 
     * @see #ATTACH_MESSAGE_ID
     */
    private static final int DISCOVER_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIDiscover", true));

    /** SWT display instance. */
    private Display display;
    /** SWT window instance. */
    private TCHAR windowClass;
    /** SWT window handle. */
    private int windowHandle;
    /** Skype Client window handle. */
    private int skypeWindowHandle;

    /**
     * Constructor.
     */
    private WindowsConnector() {
    }

    /**
     * Returns the location of Skype.exe file from the MS Windows registry
     * (implicit it check if Skype is installed or not). Checks in the registry
     * if the key: {HKCU\Software\Skype\Phone, SkypePath} exists; if not, it
     * checks again but now for {HKLM\Software\Skype\Phone, SkypePath}; if HKCU
     * key does not exist but the HKLM key is present, Skype has been installed
     * from an administrator account has but not been used from the current
     * account; otherwise there is no Skype installed.
     * 
     * @return the path to the <code>Skype.exe</code> file if Skype is
     *         installed or <code>null</code>.
     */
    @Override
    public String getInstalledPath() {
        String result = getRegistryValue(OS.HKEY_CURRENT_USER, "Software\\Skype\\Phone", "SkypePath");
        if (result == null) {
            result = getRegistryValue(OS.HKEY_LOCAL_MACHINE, "Software\\Skype\\Phone", "SkypePath");
        }
        return result;
    }

    /**
     * Returns the value to which the specified key and data is mapped in the
     * Windows registry, or null if the registry contains no mapping for this
     * key and/or data.
     * 
     * @param hKey registry hKey.
     * @param keyName registry key name.
     * @param dataName registry data name.
     * @return the value to which the specified key and data is mapped or
     *         <code>null</code>.
     */
    private String getRegistryValue(final int hKey, final String keyName, final String dataName) {
        int[] phkResult = new int[1];
        if (OS.RegOpenKeyEx(hKey, new TCHAR(0, keyName, true), 0, OS.KEY_READ, phkResult) != 0) {
            return null;
        }
        String result = null;
        int[] lpcbData = new int[1];
        if (OS.RegQueryValueEx(phkResult[0], new TCHAR(0, dataName, true), 0, null, (TCHAR) null, lpcbData) == 0) {
            result = "";
            int length = lpcbData[0] / TCHAR.sizeof;
            if (length != 0) {
                TCHAR lpData = new TCHAR(0, length);
                if (OS.RegQueryValueEx(phkResult[0], new TCHAR(0, dataName, true), 0, null, lpData, lpcbData) == 0) {
                    length = Math.max(0, lpData.length() - 1);
                    result = lpData.toString(0, length);
                }
            }
        }
        if (phkResult[0] != 0) {
            OS.RegCloseKey(phkResult[0]);
        }
        return result;
    }

    /**
     * Initialize the connector.
     * @param timeout Maximum amout of time in millieseconds to initialize.
     * @throws ConnectorException when initialization cannot be completed.
     */
    @Override
    protected void initialize() throws ConnectorException {
        final Object wait = new Object();
        final String[] errorMessage = new String[1];
        Thread thread = new Thread("SkypeEventDispatcher") {
            @Override
            public void run() {
                try {
                    display = new Display();
                    windowClass = new TCHAR(0, "" + System.currentTimeMillis() + (int) (Math.random() * 1000), true);
                    int messageReceived = new Callback(WindowsConnector.this, "messageReceived", 4).getAddress();
                    if (messageReceived == 0) {
                        setErrorMessage("The Windows connector couldn't get a callback resource.");
                        return;
                    }
                    int hHeap = OS.GetProcessHeap();
                    if (hHeap == 0) {
                        setErrorMessage("The Windows connector couldn't get the heap handle.");
                        return;
                    }
                    int hInstance = OS.GetModuleHandle(null);
                    if (hInstance == 0) {
                        setErrorMessage("The Windows connector couldn't get the module handle.");
                        return;
                    }
                    WNDCLASS lpWndClass = new WNDCLASS();
                    lpWndClass.hInstance = hInstance;
                    lpWndClass.lpfnWndProc = messageReceived;
                    lpWndClass.style = OS.CS_BYTEALIGNWINDOW | OS.CS_DBLCLKS;
                    lpWndClass.hCursor = OS.LoadCursor(0, OS.IDC_ARROW);
                    if (lpWndClass.hCursor == 0) {
                        setErrorMessage("The Windows connector couldn't get a cursor handle.");
                        return;
                    }
                    int byteCount = windowClass.length() * TCHAR.sizeof;
                    lpWndClass.lpszClassName = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
                    if (lpWndClass.lpszClassName == 0) {
                        setErrorMessage("The Windows connector couldn't get a resource.");
                        return;
                    }
                    OS.MoveMemory(lpWndClass.lpszClassName, windowClass, byteCount);
                    if (OS.RegisterClass(lpWndClass) == 0) {
                        setErrorMessage("The Windows connector couldn't register a window class.");
                        return;
                    }
                    windowHandle = OS.CreateWindowEx(0, windowClass, null, OS.WS_OVERLAPPED, 0, 0, 0, 0, 0, 0, hInstance, null);
                    if (windowHandle == 0) {
                        setErrorMessage("The Windows connector couldn't create a window.");
                        return;
                    }
                } finally {
                    synchronized (wait) {
                        wait.notify();
                    }
                }
                while (true) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            };

            private void setErrorMessage(String message) {
                errorMessage[0] = message;
            }
        };
        thread.setDaemon(true);
        synchronized (wait) {
            try {
                thread.start();
                wait.wait();
                if (errorMessage[0] != null) {
                    throw new ConnectorException(errorMessage[0]);
                }
            } catch (InterruptedException e) {
                throw new ConnectorException("The Windows connector initialization was interrupted.", e);
            }
        }
    }

    /**
     * Implementation of the connect method for this connector.
     * @param timeout maximum amout of time to connect.
     * @return Status after connecting.
     * @throws ConnectorException when connection could not be established.
     */
    protected Status connect(final int timeout) throws ConnectorException {
        final Object wait = new Object();
        ConnectorListener listener = new AbstractConnectorListener() {
            @Override
            public void statusChanged(ConnectorStatusEvent event) {
                synchronized (wait) {
                    wait.notify();
                }
            }
        };
        addConnectorListener(listener, false);
        synchronized (wait) {
            try {
                while (true) {
                    OS.SendMessage(HWND_BROADCAST, DISCOVER_MESSAGE_ID, windowHandle, 0);
                    long start = System.currentTimeMillis();
                    wait.wait(timeout);
                    if (timeout <= System.currentTimeMillis() - start) {
                        setStatus(Status.NOT_RUNNING);
                    }
                    Status status = getStatus();
                    if (status != Status.PENDING_AUTHORIZATION) {
                        return status;
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new ConnectorException("Trying to connect was interrupted.", e);
            } finally {
                removeConnectorListener(listener);
            }
        }
    }

    /**
     * Send the application name to the Skype Client.
     * @param applicationName the new applicationname.
     * @throws ConnectorException when connection to Skype client has gone bad.
     */
    protected void sendApplicationName(final String applicationName) throws ConnectorException {
        String command = "NAME " + applicationName;
        execute(command, new String[] {command}, false);
    }
    
    /**
     * Gets called when a message is received.
     * @param hwnd Skype client window handle.
     * @param msg The message received.
     * @param wParam The window parameter.
     * @param lParam The lparam.
     * @return Status value.
     */
    private int messageReceived(final int hwnd, final int msg, final int wParam, final int lParam) {
        // Using 'if' statement because ATTACH_MESSAGE_ID is not a compile time constant
        if(msg == ATTACH_MESSAGE_ID) {
            switch(lParam) {
                case ATTACH_PENDING_AUTHORIZATION:
                    setStatus(Status.PENDING_AUTHORIZATION);
                    break;
                case ATTACH_SUCCESS:
                    skypeWindowHandle = wParam;
                    setStatus(Status.ATTACHED);
                    break;
                case ATTACH_REFUSED:
                    setStatus(Status.REFUSED);
                    break;
                case ATTACH_NOT_AVAILABLE:
                    setStatus(Status.NOT_AVAILABLE);
                    break;
                case ATTACH_API_AVAILABLE:
                    setStatus(Status.API_AVAILABLE);
                    break;
                default:
                    setStatus(Status.NOT_RUNNING);
                    break;
            }
            return 1;
        } else if(msg == WM_COPYDATA) {
            if(wParam == skypeWindowHandle) {
                int[] data = new int[3];
                OS.MoveMemory(data, lParam, 12);
                int cbData = data[1];
                int lpData = data[2];
                int length = cbData;
                byte[] buffer = new byte[length];
                OS.MoveMemory(buffer, lpData, length);
                byte[] string = new byte[buffer.length - 1];
                System.arraycopy(buffer, 0, string, 0, string.length);
                try {
                    String message = new String(string, "UTF-8");
                    fireMessageReceived(message);
                    return 1;
                } catch(UnsupportedEncodingException e) {
                }
            }
        }
        return OS.DefWindowProc(hwnd, msg, wParam, lParam);
    }

    /**
     * Clean up and disconnect.
     */
    protected void disposeImpl() {
        // TODO WindowsConnector#disposeImpl()
        throw new UnsupportedOperationException("WindowsConnector#disposeImpl() is not implemented yet.");
    }

    /**
     * Send a command to the Skype client.
     * @param command The command to send.
     */
    protected void sendCommand(final String command) {
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    byte[] data = (command + "\u0000").getBytes("UTF-8");
                    int hHeap = OS.GetProcessHeap();
                    int pMessage = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, data.length);
                    OS.MoveMemory(pMessage, data, data.length);
                    OS.SendMessage(skypeWindowHandle, WM_COPYDATA, windowHandle, new int[] { 0, data.length, pMessage });
                    OS.HeapFree(hHeap, 0, pMessage);
                } catch (UnsupportedEncodingException e) {
                }
            }
        });
    }
}
