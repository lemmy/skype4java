package jp.sf.skype.sample;

import jp.sf.skype.Skype;

public class MakeCall {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java jp.sf.skype.sample.MakeCall 'skype_id'");
            return;
        }
        Skype.call(args[0]);
    }
}
