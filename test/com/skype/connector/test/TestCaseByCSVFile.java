/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/> All rights reserved.
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
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
