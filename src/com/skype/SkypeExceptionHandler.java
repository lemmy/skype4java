/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which accompanies
 * this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

/**
 * Interface to handle an uncaught {@link Throwable} in the listener's callback method. 
 * @see Skype#setSkypeExceptionHanlder()
 * @author Koji Hisano
 */
public interface SkypeExceptionHandler {
    /** 
     * Invoked when a {@link Throwable} happened in calling the listener's callback method.
     * <p>
     * Each failure of calling callback method executes this method.
     * </p>
     * @param e the throwable occured in the listener's callback method
     */
    void uncaughtExceptionHappened(Throwable e);
}
