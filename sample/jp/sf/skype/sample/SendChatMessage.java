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
package jp.sf.skype.sample;

import jp.sf.skype.Skype;

public class SendChatMessage {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java jp.sf.skype.sample.SendChatMessage 'skype_id' 'chat_message'");
            return;
        }
        Skype.chat(args[0]).send(args[1]);
    }
}
