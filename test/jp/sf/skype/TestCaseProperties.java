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

import java.util.Properties;
import junit.framework.Assert;

final class TestCaseProperties {
    private final Class testCaseClass;
    private Properties properties;

    TestCaseProperties(Class testCaseClass) {
        this.testCaseClass = testCaseClass;
        properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(getPropertyFileName()));
        } catch (Exception e) {
            Assert.fail(getPropertyFileName() + ".baseファイルを元に" + getPropertyFileName() + "ファイルを同じディレクトリ内に作成してください");
        }
    }

    private String getPropertyFileName() {
        return testCaseClass.getSimpleName() + ".properties";
    }

    String getProperty(String key) {
        if (!properties.containsKey(key)) {
            throw new IllegalArgumentException(getPropertyFileName() + "ファイルに" + key + "エントリが含まれていません");
        }
        return properties.getProperty(key);
    }
}
