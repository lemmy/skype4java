package jp.sf.skype.sample;

import jp.sf.skype.Call;
import jp.sf.skype.CallAdapter;
import jp.sf.skype.Skype;
import jp.sf.skype.SkypeException;

public class CallStopper {
    public static void main(String[] args) throws Exception {
        Skype.setDeamon(false);
        Skype.addCallListener(new CallAdapter() {
            @Override
            public void callMaked(Call makedCall) {
                try {
                    makedCall.finish();
                } catch (SkypeException e) {
                }
            }
            @Override
            public void callReceived(Call receivedCall) {
                try {
                    receivedCall.finish();
                } catch (SkypeException e) {
                }
            }
        });
    }
}
