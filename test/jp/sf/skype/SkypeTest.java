package jp.sf.skype;

import junit.framework.TestCase;

public class SkypeTest extends TestCase {
    public void testMessageReceived() throws Exception {
        final Object lock = new Object();
        final Message[] result = new Message[1];
        Skype.addMessageReceivedListener(new MessageReceivedAdapter() {
            @Override
            public void messageReceived(Message message) {
                result[0] = message;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        TestUtils.showMessageDialog("このダイアログを閉じてから1分以内に" + TestData.getFriendId() + "から'test'とメッセージを送ってもらってください");
        synchronized (lock) {
            try {
                lock.wait(60000);
            } catch (InterruptedException e) {
            }
        }
        Message message = result[0];
        assertEquals(TestData.getFriendId(), message.getPartnerId());
        assertEquals(TestData.getFriendDisplayName(), message.getPartnerDisplayName());
        assertEquals("test", message.getMessage());
    }
}
