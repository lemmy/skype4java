package jp.sf.skype.sample;

import jp.sf.skype.ChatMessage;
import jp.sf.skype.ChatMessageAdapter;
import jp.sf.skype.Skype;
import jp.sf.skype.SkypeException;

public class AutoAnswering {
    public static void main(String[] args) throws Exception {
        Skype.setDeamon(false); // to prevent exiting from this program
        Skype.addChatMessageListener(new ChatMessageAdapter() {
            public void chatMessageReceived(ChatMessage received) {
                try {
                    if (received.getType().equals(ChatMessage.Type.SAID)) {
                        received.getSender().send("I'm working. Please, wait a moment.");
                    }
                } catch (SkypeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
