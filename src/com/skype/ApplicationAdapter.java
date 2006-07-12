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
 * Bart Lamot - good javadocs
 ******************************************************************************/
package com.skype;

/**
 * Use this class to implement event triggers for AP2AP connections.
 * @author Koji Hisano
 */
public class ApplicationAdapter implements ApplicationListener {
    /**
    * Implement this method to get triggered on AP2AP connected events.
    * @param stream the stream that triggered the event.
    * @throws SkypeException when connection is gone bad.
    */
    public void connected(final Stream stream) throws SkypeException {
    }

    /**
     * Implement this method to get triggered on AP2AP disconnected events.
     * @param stream the stream that triggered the event.
     * @throws SkypeException when connection is gone bad.
     */
    public void disconnected(final Stream stream) throws SkypeException {
    }
}
