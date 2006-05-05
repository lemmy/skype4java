/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import junit.framework.Assert;

public final class TestUtils {
    public static void showCheckDialog(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
        JDialog dialog = pane.createDialog(null, "Check by the tester");
        dialog.setVisible(true);
        int result = ((Integer) pane.getValue()).intValue();
        if (result != JOptionPane.YES_OPTION) {
            Assert.fail("\"" + message + "\" -> Failed");
        }
    }

    public static void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    private TestUtils() {
    }
}
