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
package jp.sf.skype;

import java.io.File;
import junit.framework.TestCase;

public class DialogAPITest extends TestCase {
    public void testShowMainWindow() throws Exception {
        Skype.showSkypeWindow();
        TestUtils.showCheckDialog("Skype main window is showed on the top?");
        Skype.hideSkypeWindow();
        TestUtils.showCheckDialog("Skype main window is minimized?");
    }

    public void testShowAddFriendWindow() throws Exception {
        Skype.showAddFriendWindow();
        TestUtils.showCheckDialog("'Add a Contact' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        Skype.showAddFriendWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("'Add a Contact' window' is showed with " + TestData.getFriendId() + "?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowChatWindow() throws Exception {
        Skype.showChatWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("Chat window with " + TestData.getFriendId() + " is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        Skype.showChatWindow(TestData.getFriendId(), "Hello, World!");
        TestUtils.showCheckDialog("Chat window with " + TestData.getFriendId() + " which have a message 'Hello World!' is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowFileTransferWindow() throws Exception {
        Skype.showFileTransferWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("'Send file to " + TestData.getFriendId() + "' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        Skype.showFileTransferWindow(TestData.getFriendId(), new File("C:\\"));
        TestUtils.showCheckDialog("'Send file to " + TestData.getFriendId() + "' window with selecting 'C:\\' is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowProfileWindow() throws Exception {
        Skype.showProfileWindow();
        TestUtils.showCheckDialog("Profile window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowUserInformationWindow() throws Exception {
        Skype.showUserInformationWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "'s profile window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowConferenceWindow() throws Exception {
        Skype.showConferenceWindow();
        TestUtils.showCheckDialog("'Start a Skype Conference Call' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowSearchWindow() throws Exception {
        Skype.showSearchWindow();
        TestUtils.showCheckDialog("'Search for Skype Users' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowOptionsWindow() throws Exception {
        Skype.showOptionsWindow(Skype.OptionsPage.ADVANCED);
        TestUtils.showCheckDialog("Options window with selecting 'Advanced' page is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }
}
