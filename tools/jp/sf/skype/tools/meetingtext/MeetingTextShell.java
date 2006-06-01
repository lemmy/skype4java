package jp.sf.skype.tools.meetingtext;

import jp.sf.skype.SkypeException;
import jp.sf.skype.Stream;
import jp.sf.skype.StreamListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

final class MeetingTextShell extends Shell {
    private final Stream stream;
    private volatile boolean received;

    /**
     * Create the shell
     * 
     * @param display
     * @param style
     */
    public MeetingTextShell(Stream stream) {
        super(new Display(), SWT.SHELL_TRIM);
        this.stream = stream;
        createContents();
    }

    public void open() {
        super.open();
        layout();
        while (!isDisposed()) {
            if (!getDisplay().readAndDispatch()) {
                getDisplay().sleep();
            }
        }
        getDisplay().dispose();
    }

    /**
     * Create contents of the window
     */
    protected void createContents() {
        addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                try {
                    stream.write("$$end$$");
                    stream.getApplication().finish();
                    System.exit(0);
                } catch (SkypeException e1) {
                    e1.printStackTrace();
                }
            }
        });
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        setLayout(gridLayout);
        setSize(500, 375);
        setText(stream.getFriend().getId() + "と会議中");
        final Label label_2 = new Label(this, SWT.NONE);
        label_2.setText("会議テキスト(&T)");
        new Label(this, SWT.NONE);
        final StyledText styledText = new StyledText(this, SWT.MULTI
                | SWT.BORDER);
        styledText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (received) {
                    return;
                }
                try {
                    stream.write(styledText.getText());
                } catch (SkypeException e1) {
                    e1.printStackTrace();
                }
            }
        });
        stream.addStreamListener(new StreamListener() {
            public void datagramReceived(String arg0) {
            }

            public void textReceived(final String text) {
                if ("$$end$$".equals(text)) {
                    try {
                        stream.getApplication().finish();
                    } catch (SkypeException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
                received = true;
                getDisplay().syncExec(new Runnable() {
                    public void run() {
                        styledText.setText(text);
                    }
                });
                received = false;
            }
        });
        final GridData gridData = new GridData(GridData.FILL, GridData.FILL,
                true, true, 2, 1);
        gridData.widthHint = 62;
        styledText.setLayoutData(gridData);
        new Label(this, SWT.NONE);
        final Button button = new Button(this, SWT.PUSH);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    stream.getFriend().send(styledText.getText());
                } catch (SkypeException e1) {
                    e1.printStackTrace();
                }
            }
        });
        button.setLayoutData(new GridData(GridData.END, GridData.CENTER, false,
                false));
        button.setText("チャットメッセージとして送信(&S)");
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
