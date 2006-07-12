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
 * Listener used for the Application class.
 * Listener gets triggerd by AP2AP events.
 * @author Koji Hisano
 */
public interface ApplicationListener {
    /**
     * Called when a AP2AP stream is connected.
     * @param stream the connected stream.
     * @throws SkypeException when connection is gone bad.
     */
	void connected(Stream stream) throws SkypeException;
	
	/**
	 * Called when a AP2AP stream gets disconnected.
	 * @param stream the disconnected stream.
	 * @throws SkypeException when connection is gone bad.
	 */
    void disconnected(Stream stream) throws SkypeException;
}
