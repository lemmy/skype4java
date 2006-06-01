package jp.sf.skype.sample;

import jp.sf.skype.Skype;

public class SendChatMessage {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java jp.sf.skype.sample.SendChatMessage 'skype_id' 'chat_message'");
            return;
        }
        Skype.chat(args[0]).send(args[1]);
    }
}
