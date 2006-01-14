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
        TestUtils.showCheckDialog("Skypeのメインウィンドウが最前面に表示されていますか？");
        Skype.hideSkypeWindow();
        TestUtils.showCheckDialog("Skypeのメインウィンドウが最小化されていますか？");
    }

    public void testShowAddFriendWindow() throws Exception {
        Skype.showAddFriendWindow();
        TestUtils.showCheckDialog("コンタクトへ追加ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にコンタクトへ追加ウィンドウを閉じてください");
        Skype.showAddFriendWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "があらかじめ設定された状態で[コンタクトへ追加]ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前に[コンタクトへ追加]ウィンドウを閉じてください");
    }

    public void testShowChatWindow() throws Exception {
        Skype.showChatWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "とのチャットウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にチャットウィンドウを閉じてください");
        Skype.showChatWindow(TestData.getFriendId(), "Hello, World!");
        TestUtils.showCheckDialog("Hello, World!があらかじめ入力された状態で" + TestData.getFriendId() + "とのチャットウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にチャットウィンドウを閉じてください");
    }

    public void testShowFileTransferWindow() throws Exception {
        Skype.showFileTransferWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "へ送信するファイルの選択ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前に選択ウィンドウを閉じてください");
        Skype.showFileTransferWindow(TestData.getFriendId(), new File("C:\\"));
        TestUtils.showCheckDialog("C:\\があらかじめ選択された状態で" + TestData.getFriendId() + "へ送信するファイルの選択ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前に選択ウィンドウを閉じてください");
    }

    public void testShowProfileWindow() throws Exception {
        Skype.showProfileWindow();
        TestUtils.showCheckDialog("プロフィールウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にプロフィールウィンドウを閉じてください");
    }

    public void testShowUserInformationWindow() throws Exception {
        Skype.showUserInformationWindow(TestData.getFriendId());
        TestUtils.showCheckDialog(TestData.getFriendId() + "のプロフィールウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にプロフィールウィンドウを閉じてください");
    }

    public void testShowConferenceWindow() throws Exception {
        Skype.showConferenceWindow();
        TestUtils.showCheckDialog("ユーザを会議通話に招待ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にユーザを会議通話に招待ウィンドウを閉じてください");
    }

    public void testShowSearchWindow() throws Exception {
        Skype.showSearchWindow();
        TestUtils.showCheckDialog("Skypeユーザの検索ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にSkypeユーザの検索ウィンドウを閉じてください");
    }

    public void testShowOptionsWindow() throws Exception {
        Skype.showOptionsWindow(Skype.OptionsPage.ADVANCED);
        TestUtils.showCheckDialog("拡張ページがあらかじめ選択された状態で設定ウィンドウが表示されていますか？");
        TestUtils.showMessageDialog("次へ進む前にユーザを設定ウィンドウを閉じてください");
    }
}
