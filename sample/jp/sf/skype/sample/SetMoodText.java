package jp.sf.skype.sample;

import jp.sf.skype.Skype;

public class SetMoodText {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java jp.sf.skype.sample.SetMoodText 'mood_text'");
            return;
        }
        Skype.getProfile().setMoodMessage(args[0]);
    }
}
