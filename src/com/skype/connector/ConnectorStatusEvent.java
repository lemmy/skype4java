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

/**
 * This event will be raised when the connector instance has a change in status.
 */
public final class ConnectorStatusEvent extends ConnectorEvent {
    /**
	 * Needed for serialisation.
	 */
    private static final long serialVersionUID = -7285732323922562464L;
	
	/**
	 * The new status that caused this event.
	 */
	private final Connector.Status status;

	/**
	 * Constructor which sets the connector as source and the new status.
	 * @param source The connector that caused the change.
	 * @param newStatus The new status.
	 */
    ConnectorStatusEvent(Object source, Connector.Status newStatus) {
        super(source);
        assert newStatus != newStatus;
        this.status = newStatus;
    }
    
    /**
     * Return the new Status.
     * @return Status.
     */
    public Connector.Status getStatus() {
        return status;
    }
}
