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
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

/**
 * Implementation of a special USER, one that is authorized and on the contactlist.
 * @see User
 * @author Koji Hisano
 *
 */
public final class Friend extends User {
    
	/**
	 * Constuctor.
	 * @param id ID of this User.
	 */
	Friend(String id) {
        super(id);
    }
}
