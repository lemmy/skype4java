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
 ******************************************************************************/
package com.skype;

import java.io.File;

import junit.framework.TestCase;

public class DialogAPITest extends TestCase {
    public void testShowMainWindow() throws Exception {
        SkypeClient.showSkypeWindow();
        TestUtils.showCheckDialog("Skype main window is showed on the top?");
        SkypeClient.hideSkypeWindow();
        TestUtils.showCheckDialog("Skype main window is minimized?");
    }

    public void testShowAddFriendWindow() throws Exception {
        SkypeClient.showAddFriendWindow();
        TestUtils.showCheckDialog("'Add a Contact' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        SkypeClient.showAddFriendWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("'Add a Contact' window' is showed with " + TestData.getFriendId() + "?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowChatWindow() throws Exception {
        SkypeClient.showChatWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("Chat window with " + TestData.getFriendId() + " is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        SkypeClient.showChatWindow(TestData.getFriendId(), "Hello, World!");
        TestUtils.showCheckDialog("Chat window with " + TestData.getFriendId() + " which have a message 'Hello World!' is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowFileTransferWindow() throws Exception {
        SkypeClient.showFileTransferWindow(TestData.getFriendId());
        TestUtils.showCheckDialog("'Send file to " + TestData.getFriendId() + "' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
        SkypeClient.showFileTransferWindow(TestData.getFriendId(), new File("C:\\"));
        TestUtils.showCheckDialog("'Send file to " + TestData.getFriendId() + "' window with selecting 'C:\\' is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowProfileWindow() throws Exception {
        SkypeClient.showProfileWindow();
        TestUtils.showCheckDialog("Profile window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowUserInformationWindow() throws Exception {
        SkypeClient.showUserInformationWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "'s profile window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowConferenceWindow() throws Exception {
        SkypeClient.showConferenceWindow();
        TestUtils.showCheckDialog("'Start a Skype Conference Call' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowSearchWindow() throws Exception {
        SkypeClient.showSearchWindow();
        TestUtils.showCheckDialog("'Search for Skype Users' window is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }

    public void testShowOptionsWindow() throws Exception {
        SkypeClient.showOptionsWindow(SkypeClient.OptionsPage.ADVANCED);
        TestUtils.showCheckDialog("Options window with selecting 'Advanced' page is showed?");
        TestUtils.showMessageDialog("Please, close the window before the next step.");
    }
}
