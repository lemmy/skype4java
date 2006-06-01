/*******************************************************************************
 * Copyright (c) 2006 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006 UBION Inc. <http://www.ubion.co.jp/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Koji Hisano - initial API and implementation
 *******************************************************************************/
package jp.sf.skype.connector;

import jp.sf.skype.connector.Connector.Status;

public final class NotAttachedException extends ConnectorException {
    private final Status status;

    NotAttachedException(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
