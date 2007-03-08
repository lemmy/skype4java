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
package com.skype.sample;

import com.skype.MenuItem;
import com.skype.MenuItemClickEvent;
import com.skype.MenuItemListener;
import com.skype.Skype;
import com.skype.SkypeClient;
import com.skype.SkypeException;

public class ShowMenuItem {
    public static void main(String[] args) throws Exception {
        Skype.setDebug(true);
        Skype.setDeamon(false);
        
        final MenuItem item = SkypeClient.addMenuItem(MenuItem.Context.TOOLS, "Test menu", null, null, true, null, true);
        item.addMenuItemListener(new MenuItemListener() {
            public void menuItemClicked(MenuItemClickEvent event) throws SkypeException {
                System.out.println("Test menu is clicked.");
                event.getMenuItem().dispose();
            }
        });
    }
}
