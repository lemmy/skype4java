/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorException;
import jp.sf.skype.connector.ConnectorMessageReceivedListener;

public final class Skype {
    public enum OptionsPage {
        GENERAL, PRIVACY, NOTIFICATIONS, SOUNDALERTS, SOUNDDEVICES, HOTKEYS, CONNECTION, VOICEMAIL, CALLFORWARD, VIDEO, ADVANCED;
    }

    public enum Button {
        KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9, KEY_A, KEY_B, KEY_C, KEY_D, KEY_E, KEY_F, KEY_G, KEY_H, KEY_I, KEY_J, KEY_K, KEY_L, KEY_M, KEY_N, KEY_O, KEY_P, KEY_Q, KEY_R, KEY_S, KEY_T, KEY_U, KEY_V, KEY_W, KEY_X, KEY_Y, KEY_Z, KEY_SHARP(
                "#"), KEY_ASTERISK("*"), KEY_PLUS("+"), KEY_UP, KEY_DOWN, KEY_YES, KEY_NO, KEY_PAGEUP, KEY_PAGEDOWN, KEY_SKYPE;
        private String key;

        private Button() {
        }

        private Button(String key) {
            this.key = key;
        }

        private String getKey() {
            if (key != null) {
                return key;
            } else {
                return name().substring(name().indexOf('_') + 1);
            }
        }
    }

    private static ContactList contactList;
    private static Profile profile;
    private static Object chatMessageListenerMutex = new Object();
    private static ConnectorMessageReceivedListener chatMessageListener;
    private static List<ChatMessageListener> chatMessageListeners = Collections.synchronizedList(new ArrayList<ChatMessageListener>());
    private static Object callListenerMutex = new Object();
    private static ConnectorMessageReceivedListener callListener;
    private static List<CallListener> callListeners = Collections.synchronizedList(new ArrayList<CallListener>());
    private static Thread userThread;
    private static Object userThreadFieldMutex = new Object();
    
    public static void setDeamon(boolean on) {
        synchronized (userThreadFieldMutex) {
            if (!on && userThread == null) {
                userThread = new Thread("SkypeUserThread") {
                    public void run() {
                        Object wait = new Object();
                        synchronized (wait) {
                            try {
                                wait.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                };
                userThread.start();
            } else if (on && userThread != null) {
                userThread.interrupt();
                userThread = null;
            }
        }
    }

    public static void setDebug(boolean on) throws SkypeException {
        try {
            Connector.getInstance().setDebug(on);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    public static String getVersion() throws SkypeException {
        return Utils.getProperty("SKYPEVERSION");
    }
    
    public static boolean isInstalled() {
        return getInstalledPath() != null;
    }

    public static String getInstalledPath() {
        return Connector.getInstance().getInstalledPath();
    }

    public static boolean isRunning() throws SkypeException {
        try {
            return Connector.getInstance().isRunning();
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    public static void showSkypeWindow() throws SkypeException {
        Utils.executeWithErrorCheck("FOCUS");
    }

    public static void hideSkypeWindow() throws SkypeException {
        Utils.executeWithErrorCheck("MINIMIZE");
    }

    public static void showAddFriendWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN ADDAFRIEND");
    }

    public static void showAddFriendWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN ADDAFRIEND " + skypeId);
    }

    public static void showChatWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN IM " + skypeId);
    }

    public static void showChatWindow(String skypeId, String message) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.checkNotNull("message", message);
        Utils.executeWithErrorCheck("OPEN IM " + skypeId + " " + message);
    }

    public static void showFileTransferWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + skypeId);
    }

    public static void showFileTransferWindow(String skypeId, File folder) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.checkNotNull("folder", folder);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + skypeId + " IN " + folder);
    }

    public static void showFileTransferWindow(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + Skype.toCommaSeparatedString(skypeIds));
    }

    public static void showFileTransferWindow(String[] skypeIds, File folder) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.checkNotNull("folder", folder);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + Skype.toCommaSeparatedString(skypeIds) + " IN " + folder);
    }

    public static void showProfileWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN PROFILE");
    }

    public static void showUserInformationWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN USERINFO " + skypeId);
    }

    public static void showConferenceWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CONFERENCE");
    }

    public static void showSearchWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN SEARCH");
    }

    public static void showOptionsWindow(OptionsPage page) throws SkypeException {
        Utils.executeWithErrorCheck("OPEN OPTIONS " + page.toString().toLowerCase());
    }

    public static void showCallHistoryTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CALLHISTORY");
    }

    public static void showContactsTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CONTACTS");
    }

    public static void showDialPadTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN DIALPAD");
    }

    public static void showSendContactsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN SENDCONTACTS");
    }

    public static void showBlockedUsersWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN BLOCKEDUSERS");
    }

    public static void showImportContactsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN IMPORTCONTACTS");
    }

    public static void showGettingStartedWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN GETTINGSTARTED");
    }

    public static void showRequestAuthorizationWindow(String skypeId) throws SkypeException {
        Utils.executeWithErrorCheck("OPEN AUTHORIZATION " + skypeId);
    }

    public static void pressButton(Button button) throws SkypeException {
        Utils.executeWithErrorCheck("BTN_PRESSED " + button.getKey());
    }

    public static void releaseButton(Button button) throws SkypeException {
        Utils.executeWithErrorCheck("BTN_RELEASED " + button.getKey());
    }

    public static ContactList getContactList() throws SkypeException {
        if (contactList == null) {
            contactList = new ContactList();
        }
        return contactList;
    }

    public static Call call(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return call(Skype.toCommaSeparatedString(skypeIds));
    }

    public static Call call(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeId);
        try {
            String responseHeader = "CALL ";
            String response = Connector.getInstance().executeWithId("CALL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return new Call(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    public static Chat chat(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return chat(Skype.toCommaSeparatedString(skypeIds));
    }

    public static Chat chat(String skypeId) throws SkypeException {
        try {
            String responseHeader = "CHAT ";
            String response = Connector.getInstance().execute("CHAT CREATE " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return new Chat(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    public static VoiceMail leaveVoiceMail(String skypeId) throws SkypeException {
        try {
            String responseHeader = "VOICEMAIL ";
            String response = Connector.getInstance().execute("VOICEMAIL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(' ', responseHeader.length()));
            return new VoiceMail(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    public static Application addApplication(String name) throws SkypeException {
        Application application = new Application(name);
        application.initialize();
        return application;
    }

    /**
     * Gets the current audio input device of this Skype.
     * @return the audio input device name of this Skype, or <code>null</code> if the device is the default.
     * @throws SkypeException
     * @see #setAudioInputDevice(String)
     */
    public static String getAudioInputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_IN"));
    }

    /**
     * Gets the current audio output device of this Skype.
     * @return the audio output device name of this Skype, or <code>null</code> if the device is the default.
     * @throws SkypeException
     * @see #setAudioOutputDevice(String)
     */
    public static String getAudioOutputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_OUT"));
    }

    public static String getVideoDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("VIDEO_IN"));
    }

    private static String convertDefaultDeviceToNull(String deviceName) {
        if (isDefaultDevice(deviceName)) {
            return null;
        } else {
            return deviceName;
        }
    }

    private static boolean isDefaultDevice(String deviceName) {
        return "".equals(deviceName);
    }

    /**
     * Sets the current audio input device of this Skype.
     * @param deviceName the audio input device name. A <code>null</code> value means the default.
     * @throws SkypeException
     * @see #getAudioInputDevice()
     */
    public static void setAudioInputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Sets the current audio output device of this Skype.
     * @param deviceName the audio output device name. A <code>null</code> value means the default.
     * @throws SkypeException
     * @see #getAudioOutputDevice()
     */
    public static void setAudioOutputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_OUT", convertNullToDefaultDevice(deviceName));
    }

    public static void setVideoDevice(String deviceName) throws SkypeException {
        Utils.setProperty("VIDEO_IN", convertNullToDefaultDevice(deviceName));
    }

    private static String convertNullToDefaultDevice(String deviceName) {
        if (deviceName == null) {
            return "";
        } else {
            return deviceName;
        }
    }

    public static void openVideoTestWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN VIDEOTEST");
    }

    public static void openVideoOptionsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN OPTIONS VIDEO");
    }

    private Skype() {
    }

    public static synchronized Profile getProfile() {
        if (profile == null) {
            profile = new Profile();
        }
        return profile;
    }

    public static void addChatMessageListener(ChatMessageListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.add(listener);
            if (chatMessageListener == null) {
                chatMessageListener = new ConnectorMessageReceivedListener() {
                    public void messageReceived(String message) {
                        assert message != null;
                        if (message.startsWith("CHATMESSAGE ")) {
                            String data = message.substring("CHATMESSAGE ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("STATUS".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                ChatMessageListener[] listeners = chatMessageListeners.toArray(new ChatMessageListener[0]);
                                ChatMessage chatMessage = new ChatMessage(id);
                                if ("SENT".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                        listener.chatMessageSent(chatMessage);
                                    }
                                } else if ("RECEIVED".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                        listener.chatMessageReceived(chatMessage);
                                    }
                                }
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorMessageReceivedListener(chatMessageListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    public static void removeChatMessageListener(ChatMessageListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (chatMessageListenerMutex) {
            chatMessageListeners.remove(listener);
            if (chatMessageListeners.isEmpty()) {
                Connector.getInstance().removeConnectorMessageReceivedListener(chatMessageListener);
                chatMessageListener = null;
            }
        }
    }

    public static void addCallListener(CallListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
            callListeners.add(listener);
            if (callListener == null) {
                callListener = new ConnectorMessageReceivedListener() {
                    private List<String> deliveredCalls = new LinkedList<String>();
                    public void messageReceived(String receivedMessage) {
                        if (receivedMessage.startsWith("CALL ")) {
                            String data = receivedMessage.substring("CALL ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("STATUS".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                Call.Status status = Call.Status.valueOf(propertyValue);
                                switch (status) {
                                    case RINGING:
                                        synchronized(deliveredCalls) {
                                            if (!deliveredCalls.contains(id)) {
                                                deliveredCalls.add(id);
                                                Call call = new Call(id);
                                                CallListener[] listeners = callListeners.toArray(new CallListener[0]);
                                                try {
                                                    switch (call.getType()) {
                                                    case OUTGOING_P2P:
                                                    case OUTGOING_PSTN:
                                                        for (CallListener listener : listeners) {
                                                            listener.callMaked(call);
                                                        }
                                                        break;
                                                    case INCOMING_P2P:
                                                    case INCOMING_PSTN:
                                                        for (CallListener listener : listeners) {
                                                            listener.callReceived(call);
                                                        }
                                                        break;
                                                    }
                                                } catch (SkypeException e) {
                                                    // TODO add handler for exception
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        break;
                                    case FINISHED: case MISSED: case REFUSED: case CANCELLED:
                                        synchronized(deliveredCalls) {
                                            deliveredCalls.remove(id);
                                        }
                                }
                            }
                        }
                    }
                };
                try {
                    Connector.getInstance().addConnectorMessageReceivedListener(callListener);
                } catch (ConnectorException e) {
                    Utils.convertToSkypeException(e);
                }
            }
        }
    }

    public static void removeCallListener(CallListener listener) {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
            callListeners.remove(listener);
            if (callListeners.isEmpty()) {
                Connector.getInstance().removeConnectorMessageReceivedListener(callListener);
                callListener = null;
            }
        }
    }

    private static String toCommaSeparatedString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(array[i]);
        }
        return builder.toString();
    }
}
