/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.skype.connector.Connector;
import com.skype.connector.ConnectorException;
import com.skype.connector.ConnectorMessageReceivedListener;

/**
 * Main class of the Skype Java API.
 * Use this class staticly to manipulate the Skype client or send messages, SMS messages or calls.
 * @author Koji Hisano.
 */
public final class Skype {
	/**
	 * Enumeration of the OPTIONS page.
	 * Options dialog of Skype client.
	 */
    public enum OptionsPage {
    	/**
    	 * GENERAL - general options dialog.
    	 * PRIVACY - privacy options dialog.
    	 * NOTIFICATIONS - notifications options dialog.
    	 * SOUNDALERTS - soundalerts options dialog.
    	 * SOUNDDEVICES - sound devices options dialog.
    	 * HOTKEYS - hotkeys options dialog.
    	 * CONNECTION - connection options dialog.
    	 * VOICEMAIL - voicemail options dialog.
    	 * CALLFORWARD - callforward options dialog.
    	 * VIDEO - video options dialog.
    	 * ADVANCED - advanced options dialog.
    	 * 
    	 */
        GENERAL, PRIVACY, NOTIFICATIONS, SOUNDALERTS, SOUNDDEVICES, HOTKEYS, CONNECTION, VOICEMAIL, CALLFORWARD, VIDEO, ADVANCED;
    }

    /**
     * Enumeration of keypad keys.
     */
    public enum Button {
    	/**
    	 * KEY_0 - 0 on the callpad.
    	 * KEY_1 - 1 on the callpad.
    	 * KEY_2 - 2 on the callpad.
    	 * KEY_3 - 3 on the callpad.
    	 * KEY_4 - 4 on the callpad.
    	 * KEY_5 - 5 on the callpad.
    	 * KEY_6 - 6 on the callpad.
    	 * KEY_7 - 7 on the callpad.
    	 * KEY_8 - 8 on the callpad.
    	 * KEY_9 - 9 on the callpad.
    	 * KEY_A - A on the callpad.
    	 * KEY_B - B on the callpad.
    	 * KEY_C - C on the callpad.
    	 * KEY_D - D on the callpad.
    	 * KEY_E - E on the callpad.
    	 * KEY_F - F on the callpad.
    	 * KEY_G - G on the callpad.
    	 * KEY_H - H on the callpad.
    	 * KEY_I - I on the callpad.
    	 * KEY_J - J on the callpad.
    	 * KEY_K - K on the callpad.
    	 * KEY_L - L on the callpad.
    	 * KEY_M - M on the callpad.
    	 * KEY_N - N on the callpad.
    	 * KEY_O - O on the callpad.
    	 * KEY_P - P on the callpad.
    	 * KEY_Q - Q on the callpad.
    	 * KEY_R - R on the callpad.
    	 * KEY_S - S on the callpad.
    	 * KEY_T - T on the callpad.
    	 * KEY_U - U on the callpad.
    	 * KEY_V - V on the callpad.
    	 * KEY_W - W on the callpad.
    	 * KEY_X - X on the callpad.
    	 * KEY_Y - Y on the callpad.
    	 * KEY_Z - Z on the callpad.
    	 * KEY_SHARP - # on the callpad.
    	 * KEY_ASTERIX - * on the callpad.
    	 * KEY_PLUS - + on the callpad.
    	 * KEY_UP - on the callpad.
    	 * KEY_DOWN - on the callpad.
    	 * KEY_YES - yes on the callpad.
    	 * KEY_NO - no on the callpad.
    	 * KEY_PAGEUP - on the callpad.
    	 * KEY_PAGEDOW - on the callpad.
    	 * KEY_SKYPE - on the callpad.
    	 */
        KEY_0, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9, KEY_A, KEY_B, KEY_C, KEY_D, KEY_E, KEY_F, KEY_G, KEY_H, KEY_I, KEY_J, KEY_K, KEY_L, KEY_M, KEY_N, KEY_O, KEY_P, KEY_Q, KEY_R, KEY_S, KEY_T, KEY_U, KEY_V, KEY_W, KEY_X, KEY_Y, KEY_Z, KEY_SHARP("#"), KEY_ASTERISK("*"), KEY_PLUS("+"), KEY_UP, KEY_DOWN, KEY_YES, KEY_NO, KEY_PAGEUP, KEY_PAGEDOWN, KEY_SKYPE;

        /** Key instance. */
        private String key;
        
        /** Constructor. */
        private Button() {
        }

        /**
         * Constructor using a String as button reference.
         * @param newKey The button.
         */
        private Button(String newKey) {
            this.key = newKey;
        }

        /** 
         * Get the key. 
         * @return The string.
         * */
        private String getKey() {
            if (key != null) {
                return key;
            } else {
                return name().substring(name().indexOf('_') + 1);
            }
        }
    }

    /** contactList instance. */
    private static ContactList contactList;
    
    /** Profile instance for this Skype session. */
    private static Profile profile;

    /** chatMessageListener lock. */
    private static Object chatMessageListenerMutex = new Object();
    /** CHATMESSAGE listener. */
    private static ConnectorMessageReceivedListener chatMessageListener;
    /** Collection of listeners. */
    private static List<ChatMessageListener> chatMessageListeners = Collections.synchronizedList(new ArrayList<ChatMessageListener>());

    /** callListener lock object. */
    private static Object callListenerMutex = new Object();
    /** CALL listener. */
    private static ConnectorMessageReceivedListener callListener;
    /** Collection of all CALL listeners. */
    private static List<CallListener> callListeners = Collections.synchronizedList(new ArrayList<CallListener>());

    /** User thread. */
    private static Thread userThread;
    /** User threading lock object. */
    private static Object userThreadFieldMutex = new Object();

    /** General exception handler. */
    private static SkypeExceptionHandler defaultExceptionHandler = new SkypeExceptionHandler() {
        /** Print the non caught exceptions. */
    	public void uncaughtExceptionHappened(Throwable e) {
            e.printStackTrace();
        }
    };
    /** refrence to the default exception handler. */
    private static SkypeExceptionHandler exceptionHandler = defaultExceptionHandler;

    /**
     * Make the main thread of this API a deamon thread.
     * @see Thread
     * @param on if true the main thread will be a deamon thread.
     */
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

    /**
     * Enable debug logging.
     * @param on if true debug logging will be sent to the console.
     * @throws SkypeException when the connection has gone bad.
     */
    public static void setDebug(boolean on) throws SkypeException {
        try {
            Connector.getInstance().setDebug(on);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
        }
    }

    /**
     * Return the version of the Skype client (not this API).
     * @return String with version.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static String getVersion() throws SkypeException {
        return Utils.getProperty("SKYPEVERSION");
    }

    /**
     * Check if Skype client is installed on this computer.
     * WARNING, does not work for all platforms yet.
     * @return true if Skype client is installed.
     */
    public static boolean isInstalled() {
        return getInstalledPath() != null;
    }

    /**
     * Find the install path of the Skype client.
     * WARNING, does not work for all platforms yet.
     * @return String with the full path to Skype client.
     */
    public static String getInstalledPath() {
        return Connector.getInstance().getInstalledPath();
    }

    /**
     * Check if Skype client is running.
     * WARNING, does not work for all platforms.
     * @return true if Skype client is running.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static boolean isRunning() throws SkypeException {
        try {
            return Connector.getInstance().isRunning();
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return false;
        }
    }

    /**
     * Put focus on the Skype client window, not any Java window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showSkypeWindow() throws SkypeException {
        Utils.executeWithErrorCheck("FOCUS");
    }

    /**
     * Remove focus on Skype client window, not any Java window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void hideSkypeWindow() throws SkypeException {
        Utils.executeWithErrorCheck("MINIMIZE");
    }

    /**
     * Open the "Add friend" window of Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showAddFriendWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN ADDAFRIEND");
    }

    /**
     * Open the "Add friend" window if skypeId exist.
     * @param skypeId the Skype ID to check before opening the window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showAddFriendWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN ADDAFRIEND " + skypeId);
    }

    /**
     * Open a chatwindow to another Skype user.
     * @param skypeId The user to open a chat with.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showChatWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN IM " + skypeId);
    }

    /**
     * Open a chatwindow to another skype user and send a message.
     * @param skypeId The other user to chat with.
     * @param message The message to send.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showChatWindow(String skypeId, String message) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.checkNotNull("message", message);
        Utils.executeWithErrorCheck("OPEN IM " + skypeId + " " + message);
    }

    /**
     * Open File transfer window ten send a file to another Skype user.
     * @param skypeId The user to send the file to.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + skypeId);
    }

    /**
     * Show file transfer windows with a specific folder to send a file to another Skype user.
     * @param skypeId the user to send the file to.
     * @param folder the folder to show in the window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String skypeId, File folder) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.checkNotNull("folder", folder);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + skypeId + " IN " + folder);
    }

    /**
     * Show the file transfer window to send a file to several other Skype users.
     * @param skypeIds multiple Skype users to send file to.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Show file transfer window with a specific Folder to send a file to multiple Skype users.
     * @param skypeIds multiple Skype users to send file to.
     * @param folder the folder to open with the dialog.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showFileTransferWindow(String[] skypeIds, File folder) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.checkNotNull("folder", folder);
        Utils.executeWithErrorCheck("OPEN FILETRANSFER " + Utils.convertToCommaSeparatedString(skypeIds) + " IN " + folder);
    }

    /**
     * Open the Skype client profile window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showProfileWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN PROFILE");
    }

    /**
     * Open the User Information window with the info on a Skype user.
     * @param skypeId The skype user to show.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showUserInformationWindow(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeId", skypeId);
        Utils.executeWithErrorCheck("OPEN USERINFO " + skypeId);
    }

    /**
     * Open the conference window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showConferenceWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CONFERENCE");
    }

    /**
     * Open the search window of the Skype Client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showSearchWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN SEARCH");
    }

    /**
     * Open the Options window of the Skype Client.
     * @see OptionsPage
     * @param page the page to ope in front.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showOptionsWindow(OptionsPage page) throws SkypeException {
        Utils.executeWithErrorCheck("OPEN OPTIONS " + page.toString().toLowerCase());
    }

    /**
     * Focus the Call history tab of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showCallHistoryTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CALLHISTORY");
    }

    /**
     * Focus the Contacts tab of the Skype client window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showContactsTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN CONTACTS");
    }

    /**
     * Focus dialpad tab on the Skype client window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showDialPadTab() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN DIALPAD");
    }

    /**
     * Open send contacts window.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showSendContactsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN SENDCONTACTS");
    }

    /**
     * Show blacked users window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showBlockedUsersWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN BLOCKEDUSERS");
    }

    /**
     * Show import contacts window of Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showImportContactsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN IMPORTCONTACTS");
    }

    /**
     * Show the getting started window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showGettingStartedWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN GETTINGSTARTED");
    }

    /**
     * Show the request authorisation window for a Skype user ID.
     * @param skypeId the User to ask authorisation for.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void showRequestAuthorizationWindow(String skypeId) throws SkypeException {
        Utils.executeWithErrorCheck("OPEN AUTHORIZATION " + skypeId);
    }

    /**
     * Press a button in the Skype client window.
     * @see Button
     * @param button The button to press.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void pressButton(Button button) throws SkypeException {
        Utils.executeWithErrorCheck("BTN_PRESSED " + button.getKey());
    }

    /**
     * Release a pressed button in the Skype client window.
     * @see Button
     * @param button the button to release.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void releaseButton(Button button) throws SkypeException {
        Utils.executeWithErrorCheck("BTN_RELEASED " + button.getKey());
    }

    /**
     * Get the contactlist instance of this Skype session.
     * @return contactlist singleton.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static ContactList getContactList() throws SkypeException {
        if (contactList == null) {
            contactList = new ContactList();
        }
        return contactList;
    }

    /**
     * Make a Skype CALL to multiple users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to call.
     * @return The started Call.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Call call(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return call(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Make a Skype CALL to one single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to call.
     * @return The new call object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Call call(String skypeId) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeId);
        try {
            String responseHeader = "CALL ";
            String response = Connector.getInstance().executeWithId("CALL " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return Call.getCall(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Start a chat with multiple Skype users.
     * Without using the Skype client dialogs.
     * @param skypeIds The users to start a chat with.
     * @return The new chat object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Chat chat(String[] skypeIds) throws SkypeException {
        Utils.checkNotNull("skypeIds", skypeIds);
        return chat(Utils.convertToCommaSeparatedString(skypeIds));
    }

    /**
     * Start a chat with a single Skype user.
     * Without using the Skype client dialogs.
     * @param skypeId The user to start the with.
     * @return The new chat.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Chat chat(String skypeId) throws SkypeException {
        try {
            String responseHeader = "CHAT ";
            String response = Connector.getInstance().executeWithId("CHAT CREATE " + skypeId, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return new Chat(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param numbers the cell phone numbers to validate.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String[] numbers) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        return submitConfirmationCode(Utils.convertToCommaSeparatedString(numbers));
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param number the cell phone numbers to validate.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String number) throws SkypeException {
        SMS message = createSMS(number, SMS.Type.CONFIRMATION_CODE_REQUEST);
        message.send();
        return message;
    }

    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param numbers the cell phone numbers to validate.
     * @param code the validation code to send.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String[] numbers, String code) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        Utils.checkNotNull("code", code);
        return submitConfirmationCode(Utils.convertToCommaSeparatedString(numbers), code);
    }
    
    /**
     * Send a SMS confirmation code.
     * An outgoing SMS message from Skype lists the reply-to number as the user's 
     * Skype ID. It is possible to change the reply-to number to a mobile phone number by
     * registering the number in the Skype client. Skype validates the number and this
     * number then becomes the reply-to number for outgoing SMS messages.
     * @param number the cell phone numbers to validate.
     * @param code the validation code to send.
     * @return A new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS submitConfirmationCode(String number, String code) throws SkypeException {
        Utils.checkNotNull("number", number);
        Utils.checkNotNull("code", code);
        SMS message = createSMS(number, SMS.Type.CONFIRMATION_CODE_REQUEST);
        message.setContent(code);
        message.send();
        return message;
    }

    /**
     * Send an SMS to one or more cell phone numbers.
     * @param numbers the cell phone numbers to send to.
     * @param content the message to send.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static SMS sendSMS(String[] numbers, String content) throws SkypeException {
        Utils.checkNotNull("numbers", numbers);
        return sendSMS(Utils.convertToCommaSeparatedString(numbers), content);
    }

    /**
     * Send an SMS to one cell phone number.
     * @param number the cell phone numbers to send to.
     * @param content the message to send.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */    
    public static SMS sendSMS(String number, String content) throws SkypeException {
        Utils.checkNotNull("number", number);
        Utils.checkNotNull("content", content);
        SMS message = createSMS(number, SMS.Type.OUTGOING);
        message.setContent(content);
        message.send();
        return message;
    }

    /**
     * Create a new SMS object to send later using SMS.send .
     * @param number Cell phone number to send it to.
     * @param type The type of SMS message.
     * @return The new SMS object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    private static SMS createSMS(String number, SMS.Type type) throws SkypeException {
        try {
            String responseHeader = "SMS ";
            String response = Connector.getInstance().executeWithId("CREATE SMS " + type + " " + number, responseHeader);
            Utils.checkError(response);
            String id = response.substring(responseHeader.length(), response.indexOf(" STATUS "));
            return new SMS(id);
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Find all SMS messages.
     * @return Array of SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public SMS[] getAllSMSs() throws SkypeException {
        return getAllSMSs("SMSS");
    }

    /**
     * Find all missed SMS messages.
     * @return Array of SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public SMS[] getAllMissedSMSs() throws SkypeException {
        return getAllSMSs("MISSEDSMSS");
    }

    /**
     * Find all SMS message of a certain type.
     * @param type The type to search for.
     * @return Array of found SMS messages.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    private SMS[] getAllSMSs(String type) throws SkypeException {
        try {
            String command = "SEARCH " + type;
            String responseHeader = type + " ";
            String response = Connector.getInstance().execute(command, responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            SMS[] smss = new SMS[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                smss[i] = new SMS(ids[i]);
            }
            return smss;
        } catch (ConnectorException e) {
            Utils.convertToSkypeException(e);
            return null;
        }
    }

    /**
     * Leave a voicemail in a other Skype users voicemailbox.
     * @param skypeId The Skype user to leave a voicemail.
     * @return The new Voicemail object.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
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

    /**
     * Add an AP2AP capable application.
     * @param name The name of the AP2AP application.
     * @return Application object reference.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static Application addApplication(String name) throws SkypeException {
        Application application = new Application(name);
        application.initialize();
        return application;
    }

    /**
     * Gets the current audio input device of this Skype.
     * 
     * @return the audio input device name of this Skype, or <code>null</code>
     *         if the device is the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #setAudioInputDevice(String)
     */
    public static String getAudioInputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_IN"));
    }

    /**
     * Gets the current audio output device of this Skype.
     * 
     * @return the audio output device name of this Skype, or <code>null</code>
     *         if the device is the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #setAudioOutputDevice(String)
     */
    public static String getAudioOutputDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("AUDIO_OUT"));
    }

    /**
     * Get the current video input device used by the Skype Client.
     * @return String with the device name or null if there isn't one.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static String getVideoDevice() throws SkypeException {
        return convertDefaultDeviceToNull(Utils.getProperty("VIDEO_IN"));
    }

    /**
     * Return null if the default device is used.
     * @param deviceName Name of the device to check.
     * @return <code>null</code> if device is default else devicename.
     */
    private static String convertDefaultDeviceToNull(String deviceName) {
        if (isDefaultDevice(deviceName)) {
            return null;
        } else {
            return deviceName;
        }
    }

    /**
     * Compare the devicename to the default value.
     * @param deviceName the string to compare.
     * @return true if devicename is equal to defaultname.
     */
    private static boolean isDefaultDevice(String deviceName) {
        return "".equals(deviceName);
    }

    /**
     * Sets the current audio input device of this Skype.
     * 
     * @param deviceName
     *            the audio input device name. A <code>null</code> value means
     *            the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #getAudioInputDevice()
     */
    public static void setAudioInputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Sets the current audio output device of this Skype.
     * 
     * @param deviceName
     *            the audio output device name. A <code>null</code> value
     *            means the default.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     * @see #getAudioOutputDevice()
     */
    public static void setAudioOutputDevice(String deviceName) throws SkypeException {
        Utils.setProperty("AUDIO_OUT", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Set the video device used by the Skype client.
     * @param deviceName name of the device to set.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void setVideoDevice(String deviceName) throws SkypeException {
        Utils.setProperty("VIDEO_IN", convertNullToDefaultDevice(deviceName));
    }

    /**
     * Convert a <code>null</code> to the default device.
     * @param deviceName String to convert.
     * @return Default device string.
     */
    private static String convertNullToDefaultDevice(String deviceName) {
        if (deviceName == null) {
            return "";
        } else {
            return deviceName;
        }
    }

    /**
     * Open the Test video dialog of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void openVideoTestWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN VIDEOTEST");
    }

    /**
     * Open the Video options window of the Skype client.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void openVideoOptionsWindow() throws SkypeException {
        Utils.executeWithErrorCheck("OPEN OPTIONS VIDEO");
    }

    /**
     * Get the singleton instance of the users profile.
     * @return Profile.
     */
    public static synchronized Profile getProfile() {
        if (profile == null) {
            profile = new Profile();
        }
        return profile;
    }

    /**
     * Gets the recent chats in the locally-cached history.
     *
     * @return The recent chats in the locally-cached history
     *
     * @throws SkypeException If there is a problem with the connection or state at the Skype client.
     */
    public static Chat[] getRecentChats() throws SkypeException {
        try {
            String responseHeader = "CHATS ";
            String response = Connector.getInstance().execute("SEARCH RECENTCHATS", responseHeader);
            String data = response.substring(responseHeader.length());
            String[] ids = Utils.convertToArray(data);
            Chat[] chats = new Chat[ids.length];
            for (int i = 0; i < ids.length; ++i) {
                chats[i] = new Chat(ids[i]);
            }
            return chats;
        } catch (ConnectorException ex) {
            Utils.convertToSkypeException(ex);
            return null;
        }
    }

    /**
     * Add a listener for CHATMESSAGE events received from the Skype API.
     * @see ChatMessageListener
     * @param listener the Listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
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
                                        try {
                                            listener.chatMessageSent(chatMessage);
                                        } catch (Throwable e) {
                                            handleUncaughtException(e);
                                        }
                                    }
                                } else if ("RECEIVED".equals(propertyValue)) {
                                    for (ChatMessageListener listener : listeners) {
                                        try {
                                            listener.chatMessageReceived(chatMessage);
                                        } catch (Throwable e) {
                                            handleUncaughtException(e);
                                        }
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

    /**
     * Remove a listener for CHATMESSAGE events.
     * If the listener is already removed nothing happens.
     * @param listener The listener to remove.
     */
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

    /**
     * Add a listener for CALL events received from the Skype API.
     * @see CallListener
     * @param listener the listener to add.
     * @throws SkypeException when connection has gone bad or ERROR reply.
     */
    public static void addCallListener(CallListener listener) throws SkypeException {
        Utils.checkNotNull("listener", listener);
        synchronized (callListenerMutex) {
            callListeners.add(listener);
            if (callListener == null) {
                callListener = new ConnectorMessageReceivedListener() {
                    public void messageReceived(String receivedMessage) {
                        if (receivedMessage.startsWith("CALL ")) {
                            String data = receivedMessage.substring("CALL ".length());
                            String id = data.substring(0, data.indexOf(' '));
                            String propertyNameAndValue = data.substring(data.indexOf(' ') + 1);
                            String propertyName = propertyNameAndValue.substring(0, propertyNameAndValue.indexOf(' '));
                            if ("STATUS".equals(propertyName)) {
                                String propertyValue = propertyNameAndValue.substring(propertyNameAndValue.indexOf(' ') + 1);
                                Call.Status status = Call.Status.valueOf(propertyValue);
                                Call call = Call.getCall(id);
                                EXIT: if (status == Call.Status.RINGING) {
                                    synchronized(call) {
                                        if (call.isCallListenerEventFired()) {
                                            break EXIT;
                                        }
                                        call.setCallListenerEventFired(true);
                                        CallListener[] listeners = callListeners.toArray(new CallListener[0]);
                                        try {
                                            switch (call.getType()) {
                                                case OUTGOING_P2P:
                                                case OUTGOING_PSTN:
                                                    for (CallListener listener : listeners) {
                                                        try {
                                                            listener.callMaked(call);
                                                        } catch (Throwable e) {
                                                            handleUncaughtException(e);
                                                        }
                                                    }
                                                    break;
                                                case INCOMING_P2P:
                                                case INCOMING_PSTN:
                                                    for (CallListener listener : listeners) {
                                                        try {
                                                            listener.callReceived(call);
                                                        } catch (Throwable e) {
                                                            handleUncaughtException(e);
                                                        }
                                                    }
                                                    break;
                                                default: 
                                                	//Should an exception be thrown?
                                                	break;
                                            }
                                        } catch (Throwable e) {
                                            handleUncaughtException(e);
                                        }
                                    }
                                }
                                call.fireStatusChanged(status);
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

    /**
     * Remove a listener for CALL events.
     * If listener is already removed nothing happens.
     * @param listener The listener to add.
     */
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

    /**
     * Use another exceptionhandler then the default one.
     * @see SkypeExceptionHandler
     * @param handler the handler to use.
     */
    public static void setSkypeExceptionHandler(SkypeExceptionHandler handler) {
        if (handler == null) {
            handler = defaultExceptionHandler;
        }
        exceptionHandler = handler;
    }

    /**
     * Handle uncaught exceptions in a default way.
     * @param e the uncaught exception.
     */
    static void handleUncaughtException(Throwable e) {
        exceptionHandler.uncaughtExceptionHappened(e);
    }

    /** 
     * Private constructor.
     * Please use this object staticly.
     *
     */
    private Skype() {
    }
}
