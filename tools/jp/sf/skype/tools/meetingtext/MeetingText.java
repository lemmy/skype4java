package jp.sf.skype.tools.meetingtext;

import jp.sf.skype.Application;
import jp.sf.skype.ApplicationListener;
import jp.sf.skype.ChatMessage;
import jp.sf.skype.ChatMessageAdapter;
import jp.sf.skype.ChatMessageListener;
import jp.sf.skype.Skype;
import jp.sf.skype.SkypeException;
import jp.sf.skype.Stream;

/**
 * アプリケーションの起動クラスを定義します。
 */
public final class MeetingText {
    /**
     * アプリケーションを起動します。
     * 
     * @param args
     *            起動引数を指定します。
     */
    public static void main(String[] args) throws Exception {
        Skype.setDebug(true);
        Skype.setDeamon(false);
        final Application application = Skype.addApplication("conference");
        application.addApplicationListener(new ApplicationListener() {
            public void disconnected(Stream stream) {
            }

            public void connected(final Stream stream) {
                new Thread() {
                    public void run() {
                        try {
                            new MeetingTextShell(stream).open();
                        } catch (Exception e) {
                            Utils.openErrorMessageDialog("起動に失敗",
                                    "ミーティングテキストツールの起動に失敗しました。\n原因: "
                                            + e.getMessage());
                        }
                    }
                }.start();
            }
        });
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            public void chatMessageReceived(final ChatMessage message) {
                try {
                    if (message.getContent().contains("「会議」")) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    application.connect(Skype.getContactList()
                                            .getFriend(message.getSenderId()));
                                } catch (Exception e) {
                                    Utils
                                            .openErrorMessageDialog("接続に失敗",
                                                    "相手がミーティングテキストツールをインストールしていないため接続できませんでした。");
                                }
                            }
                        }.start();
                    } else if (message.getContent().contains("「終了」")) {
                        Skype.setDeamon(true);
                    }
                } catch (final SkypeException e) {
                    new Thread() {
                        @Override
                        public void run() {
                            Utils.openErrorMessageDialog("プラグインエラー",
                                    "プラグインの内部エラーが発生しました。\n原因: "
                                            + e.getMessage());
                        };
                    }.start();
                }
            }
        });
        if (args.length == 1) {
            Skype.getContactList().getFriend(args[0]).send("「会議」");
        }
    }
}
