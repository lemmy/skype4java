/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype.connector.test;

import java.io.File;

import junit.framework.TestCase;

public abstract class TestCaseByCSVFile extends TestCase {
    private boolean isRecordingMode;
    private CSVRecorder recorder;
    
    public final void setRecordingMode(final boolean on) {
        isRecordingMode = on;
    }
    
    public final boolean isRecordingMode() {
        return isRecordingMode;
    }
    
    @Override
    protected final void runTest() throws Throwable {
        if (isRecordingMode) {
            recorder = startCSVRecording();
        } else {
            startCSVPlaying();
        }
        try {
            super.runTest();
        } finally {
            if (isRecordingMode) {
                endCSVRecording(recorder);
            } else {
                endCSVPlaying();
            }
        }
    }

    private CSVRecorder startCSVRecording() throws Exception {
        CSVRecorder recorder = new CSVRecorder(getTestDataFileName());
        TestConnector.getInstance().addRecorder(recorder);
        return recorder;
    }

    private void endCSVRecording(final CSVRecorder recorder) {
        assert recorder != null;
        recorder.close();
    }
    
    private void startCSVPlaying() throws Exception {
        TestConnector.getInstance().setPlayer(new CSVPlayer(getTestDataFileName()));
    }
    
    private void endCSVPlaying() throws Exception {
        TestConnector.getInstance().clearPlayer();
    }

    private String getTestDataFileName() {
        return "test" + File.separator + getClass().getName().replace(".", File.separator) + "_" + getName() + ".csv";
    }
}
