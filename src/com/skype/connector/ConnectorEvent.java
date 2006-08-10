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

import java.util.Date;
import java.util.EventObject;

/**
 * Event will by raised when a connector has a event.
 */
class ConnectorEvent extends EventObject {
    /**
	 * Needed for all serialization classes. 
	 */
	private static final long serialVersionUID = 1L;
	
	/** Time. */
	private final long time;

	/**
	 * Constructor.
	 * @param source The event source.
	 */
    ConnectorEvent(final Object source) {
        super(source);
        assert source != null;
        time = System.currentTimeMillis();
    }

    /**
     * Get the source Connector.
     * @return Connector.
     */
    public final Connector getConnector() {
        return (Connector)getSource();
    }
    
    /**
     * Get the time of the event.
     * @return Date fo event.
     */
    public final Date getTime() {
        return new Date(time);
    }
}
