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
package com.skype.connector;

import com.skype.connector.Connector.Status;

/**
 * This exception will be thrown when a command is sent to a connector but when the connector isn't connected.
 */
public final class NotAttachedException extends ConnectorException {
    /**
	 * Needed for serialisation.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The actual current status (Not attached).
	 */
	private final Status status;

	/**
	 * Constructor with the current status.
	 * @param newStatus Current status.
	 */
    NotAttachedException(Status newStatus) {
        this.status = newStatus;
    }

    /**
     * Return the status at the moment of the exception.
     * @return Status.
     */
    public Status getStatus() {
        return status;
    }
}
