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

public abstract class Recorder {
    private long startTime;
    
    final boolean isStarted() {
        return startTime != 0;
    }
    
    final void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    final long getStartTime() {
        return startTime;
    }
    
    protected abstract void recordSentMessage(long time, String message);
    protected abstract void recordReceivedMessage(long time, String message);
}
