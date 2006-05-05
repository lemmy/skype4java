package jp.sf.skype.win32;

import jp.sf.skype.TestUtils;
import jp.sf.skype.connector.Connector;
import jp.sf.skype.connector.ConnectorStatusChangedListener;
import jp.sf.skype.connector.Connector.Status;
import jp.sf.skype.connector.windows.WindowsConnector;
import junit.framework.TestCase;

public final class WindowsConnectorTest extends TestCase {
    public void testAttachedAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Open [Tools - Options - Privacy - Manage other programs' access to Skype] and ensure that there is no java application (JAVAW.EXE or JAVA.EXE), please.");
        TestUtils.showMessageDialog("Select [Another program wants to use Skype - Allow this program to use Skype] when Skype shows the authorization dialog, please.");
        assertEquals(Connector.Status.ATTACHED, WindowsConnector.getInstance().connect());
    }

    public void testRefusedAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Open [Tools - Options - Privacy - Manage other programs' access to Skype] and ensure that there is no java application (JAVAW.EXE or JAVA.EXE), please.");
        TestUtils.showMessageDialog("Select [Another program wants to use Skype - Do not allow this program to use Skype] when Skype shows the authorization dialog, please.");
        assertEquals(Connector.Status.REFUSED, WindowsConnector.getInstance().connect());
        TestUtils.showMessageDialog("Open [Tools - Options - Privacy - Manage other programs' access to Skype] and ensure that there is no java application (JAVAW.EXE or JAVA.EXE) for next test, please.");
    }

    public void testNotAvailableAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Sign out from Skype, please.");
        assertEquals(Connector.Status.NOT_AVAILABLE, WindowsConnector.getInstance().connect());
        
        final boolean[] available = new boolean[1];
        ConnectorStatusChangedListener listener = new ConnectorStatusChangedListener() {
            public void statusChanged(Status newStatus) {
                if (newStatus == Connector.Status.API_AVAILABLE) {
                    available[0] = true;
                }
            }
        };
        WindowsConnector.getInstance().addConnectorStatusChangedListener(listener);
        TestUtils.showMessageDialog("Sign in Skype, please.");
        assertTrue(available[0]);
    }

    public void testNotRunnigAfterTryingToConnect() throws Exception {
        TestUtils.showMessageDialog("Shut down Skype, please.");
        assertEquals(Connector.Status.NOT_RUNNING, WindowsConnector.getInstance().connect());
        TestUtils.showMessageDialog("Launch Skype for next test, please.");
    }

    public void testGetInstalledPath() {
        assertEquals("C:\\Program Files\\Skype\\Phone\\Skype.exe", WindowsConnector.getInstance().getInstalledPath());
    }
}
