package jp.sf.skype.tools.meetingtext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

final class Utils {
    static void openErrorMessageDialog(String title, String content) {
        Shell shell = new Shell(new Display());
        MessageBox errorDialog = new MessageBox(shell, SWT.ICON_ERROR);
        errorDialog.setText(title);
        errorDialog.setMessage(content);
        errorDialog.open();
        shell.getDisplay().dispose();
    }
    
    private Utils() {
    }
}
