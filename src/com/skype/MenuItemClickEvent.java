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
 * Contributors: Koji Hisano - initial API and implementation
 ******************************************************************************/
package com.skype;

import java.util.EventObject;

import com.skype.MenuItem.Context;

public final class MenuItemClickEvent extends EventObject {
    private static final long serialVersionUID = -1777142015080318057L;

    private final String[] _skypeIds;
    private final Context _context;
    private final String[] _contextIds;

    MenuItemClickEvent(MenuItem menuItem, String[] skypeIds, Context context, String[] contextIds) {
        super(menuItem);
        Utils.checkNotNull("menuItem", menuItem);
        Utils.checkNotNull("skypeIds", skypeIds);
        Utils.checkNotNull("context", context);
        Utils.checkNotNull("contextIds", contextIds);
        _skypeIds = skypeIds;
        _context = context;
        _contextIds = contextIds;
    }

    public MenuItem getMenuItem() {
        return (MenuItem) getSource();
    }

    public String[] getSkypeIds() {
        return _skypeIds;
    }

    public Context getContext() {
        return _context;
    }

    public String[] getContextIds() {
        return _contextIds;
    }
}
