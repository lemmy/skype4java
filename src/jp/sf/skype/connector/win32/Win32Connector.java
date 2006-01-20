/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype.connector.win32;

import java.io.UnsupportedEncodingException;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;
import jp.sf.skype.connector.ConnectorListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.internal.win32.WNDCLASS;
import org.eclipse.swt.widgets.Display;

public final class Win32Connector extends Connector {
    private static final int ATTACH_SUCCESS = 0;
    private static final int ATTACH_PENDING_AUTHORIZATION = 1;
    private static final int ATTACH_REFUSED = 2;
    private static final int ATTACH_NOT_AVAILABLE = 3;
    private static final int HWND_BROADCAST = 0xffff;
    private static final int WM_COPYDATA = 0x004a;
    private static final int ATTACH_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIAttach", true));
    private static final int DISCOVER_MESSAGE_ID = OS.RegisterWindowMessage(new TCHAR(0, "SkypeControlAPIDiscover", true));

    private Display display;
    private TCHAR windowClass;
    private int windowHandle;
    private int skypeWindowHandle;
    private Status status = Status.NOT_RUNNING;

    public Win32Connector() {
        initResource();
    }

    @Override
    public String getInstalledPath() {
        return getHKCUValue("Software\\Skype\\Phone", "SkypePath");
    }

    private String getHKCUValue(String keyName, String dataName) {
        TCHAR key = new TCHAR(0, keyName, true);
        int[] phkResult = new int[1];
        if (OS.RegOpenKeyEx(OS.HKEY_CURRENT_USER, key, 0, OS.KEY_READ, phkResult) != 0) {
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
        if (phkResult[0] != 0)
            OS.RegCloseKey(phkResult[0]);
        return result;
    }

    private void initResource() {
        final Object object = new Object();
        Thread thread = new Thread("Win32EventDispatcher") {
            public void run() {
                display = new Display();
                windowClass = new TCHAR(0, "" + System.currentTimeMillis() + (int) (Math.random() * 1000), true);
                int messageReceived = new Callback(Win32Connector.this, "messageReceived", 4).getAddress();
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
                object.wait(1000);
            } catch (InterruptedException e) {
                throw new InternalError("win32 connector is not initialized");
            }
        }
    }

    protected Status connect(int timeOut) throws ConnectorException {
        if (getStatus() != Status.ATTACHED) {
            initConnection(timeOut);
        }
        return getStatus();
    }

    private void initConnection(int timeOut) throws ConnectorException {
        final Object object = new Object();
        ConnectorListener listener = new ConnectorListener() {
            public void messageReceived(String message) {
                if (message.startsWith("TRIED TO ATTACH")) {
                    removeConnectorListener(this);
                    synchronized (object) {
                        object.notify();
                    }
                }
            }
        };
        addConnectorListener(listener, false);
        synchronized (object) {
            OS.SendMessage(HWND_BROADCAST, DISCOVER_MESSAGE_ID, windowHandle, 0);
            try {
                object.wait(timeOut);
            } catch (InterruptedException e) {
                if (status != Status.PENDING_AUTHORIZATION) {
                    status = Status.NOT_RUNNING;
                }
            }
        }
    }

    int messageReceived(int hwnd, int msg, int wParam, int lParam) {
        if (msg == ATTACH_MESSAGE_ID) {
            if (lParam == ATTACH_PENDING_AUTHORIZATION) {
                status = Status.PENDING_AUTHORIZATION;
                return 1;
            }
            switch (lParam) {
                case ATTACH_SUCCESS:
                    skypeWindowHandle = wParam;
                    status = Status.ATTACHED;
                    break;
                case ATTACH_REFUSED:
                    status = Status.REFUSED;
                    break;
                case ATTACH_NOT_AVAILABLE:
                    status = Status.NOT_AVAILABLE;
                    break;
            }
            fireMessageReceived("TRIED TO ATTACH");
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
    public Status getStatus() {
        return status;
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
