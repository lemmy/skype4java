/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *     Kamil Sarelo - modified getInstalledPath() to support installing by an administrator account and added some JavaDoc
 *******************************************************************************/
package jp.sf.skype.connector.windows;

import java.io.UnsupportedEncodingException;

import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;
import jp.sf.skype.connector.ConnectorMessageReceivedListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.internal.win32.WNDCLASS;
import org.eclipse.swt.widgets.Display;

public final class WindowsConnector extends Connector {
    /**
     * The singleton instance of the WindowsConnector class.
     */
    private static WindowsConnector instance;

    /**
     * Gets the singleton instance of the WindowsConnector class.
     * 
     * @return the singleton instance of the WindowsConnector class
     */
    public static synchronized WindowsConnector getInstance() {
        if (instance == null) {
            instance = new WindowsConnector();
        }
        return instance;
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
    private static final int ATTACH_API_AVAILABLE = 0x8001;
    
    /**
     * The window handle indicating all top-level windows in the system.
     * @see <a href="http://msdn.microsoft.com/library/default.asp?url=/library/en-us/winui/winui/windowsuserinterface/windowing/messagesandmessagequeues/messagesandmessagequeuesreference/messagesandmessagequeuesfunctions/sendmessage.asp">MSDN Library</a>
     */
    private static final int HWND_BROADCAST = 0xffff;
    /**
     * The window message type to pass data to another application.
     * @see <a href="http://search.msdn.microsoft.com/search/Redirect.aspx?title=WM_COPYDATA+Message&url=http://msdn.microsoft.com/library/en-us/winui/winui/windowsuserinterface/dataexchange/datacopy/datacopyreference/datacopymessages/wm_copydata.asp">MSDN Library</a>
     */
    private static final int WM_COPYDATA = 0x004a;

    /**
     * The window message type of the response for initiating communication from Skype
     * @see #DISCOVER_MESSAGE_ID
     */
    private static final int ATTACH_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIAttach", true));
    /**
     * The window message type of the request to initiate communication with Skype
     * @see #ATTACH_MESSAGE_ID
     */
    private static final int DISCOVER_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIDiscover", true));

    private static final String CONNECTOR_STATUS_CHANGED_MESSAGE = "ConnectorStatusChanged";

    private Display display;
    private TCHAR windowClass;
    private int windowHandle;
    private int skypeWindowHandle;

    private WindowsConnector() {
    }

    /**
     * Returns the location of Skype.exe file from the MS Windows registry (implicit it check if Skype is installed or not).
     * Checks in the registry if the key: {HKCU\Software\Skype\Phone, SkypePath} exists;
     * if not, it checks again but now for {HKLM\Software\Skype\Phone, SkypePath};
     * if HKCU key does not exist but the HKLM key is present, Skype has been installed from an administrator account has but not been used from the current account;
     * otherwise there is no Skype installed.
     * 
     * @return   the path to the <code>Skype.exe</code> file if Skype is installed or <code>null</code>.
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
     * Returns the value to which the specified key and data is mapped in the Windows registry, or null if the registry contains no mapping for this key and/or data.
     * 
     * @return   the value to which the specified key and data is mapped or <code>null</code>.
     */
    private String getRegistryValue(int hKey, String keyName, String dataName) {
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

    @Override
    protected void initialize(int timeout) throws ConnectorException {
        final Object object = new Object();
        Thread thread = new Thread("SkypeEventDispatcher") {
            public void run() {
                display = new Display();
                windowClass = new TCHAR(0, "" + System.currentTimeMillis() + (int) (Math.random() * 1000), true);
                int messageReceived = new Callback(WindowsConnector.this, "messageReceived", 4).getAddress();
                if (messageReceived == 0) {
                    SWT.error(SWT.ERROR_NO_MORE_CALLBACKS);
                }
                int hHeap = OS.GetProcessHeap();
                int hInstance = OS.GetModuleHandle(null);
                WNDCLASS lpWndClass = new WNDCLASS();
                lpWndClass.hInstance = hInstance;
                lpWndClass.lpfnWndProc = messageReceived;
                lpWndClass.style = OS.CS_BYTEALIGNWINDOW | OS.CS_DBLCLKS;
                lpWndClass.hCursor = OS.LoadCursor(0, OS.IDC_ARROW);
                int byteCount = windowClass.length() * TCHAR.sizeof;
                lpWndClass.lpszClassName = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
                OS.MoveMemory(lpWndClass.lpszClassName, windowClass, byteCount);
                OS.RegisterClass(lpWndClass);
                windowHandle = OS.CreateWindowEx(0, windowClass, null, OS.WS_OVERLAPPED, 0, 0, 0, 0, 0, 0, hInstance, null);
                synchronized (object) {
                    object.notify();
                }
                while (true) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            };
        };
        thread.setDaemon(true);
        thread.start();
        synchronized (object) {
            try {
                long start = System.currentTimeMillis();
                object.wait(timeout);
                if (timeout <= System.currentTimeMillis() - start) {
                    throw new ConnectorException("The Windows connector couldn't be initialized by timeout.");
                }
            } catch (InterruptedException e) {
                throw new ConnectorException("The Windows connector initialization was interrupted.", e);
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
        synchronized (object) {
            try {
                while (true) {
                    OS.SendMessage(HWND_BROADCAST, DISCOVER_MESSAGE_ID, windowHandle, 0);
                    long start = System.currentTimeMillis();
                    object.wait(timeout);
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
                removeConnectorMessageReceivedListener(listener);
            }
        }
    }

    int messageReceived(int hwnd, int msg, int wParam, int lParam) {
        if (msg == ATTACH_MESSAGE_ID) {
            switch (lParam) {
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
            }
            fireMessageReceived(CONNECTOR_STATUS_CHANGED_MESSAGE);
            return 1;
        } else if (msg == WM_COPYDATA) {
            if (wParam == skypeWindowHandle) {
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
                } catch (UnsupportedEncodingException e) {
                }
            }
        }
        return OS.DefWindowProc(hwnd, msg, wParam, lParam);
    }

    @Override
    protected void disposeImpl() {
        // TODO WindowsConnector#disposeImpl()
        throw new UnsupportedOperationException("WindowsConnector#disposeImpl() is not implemented yet.");
    }

    @Override
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
