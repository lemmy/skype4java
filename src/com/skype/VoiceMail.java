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
package com.skype;

import java.util.HashMap;
import java.util.Map;

public class VoiceMail extends SkypeObject {
    /**
     * Collection of VoiceMail objects.
     */
    private static final Map<String, VoiceMail> voiceMails = new HashMap<String, VoiceMail>();
    
    /**
     * Returns the VoiceMail object by the specified id.
     * @param id whose associated VoiceMail object is to be returned.
     * @return VoiceMail object with ID == id.
     */
    static VoiceMail getInstance(final String id) {
        synchronized(voiceMails) {
            if (!voiceMails.containsKey(id)) {
                voiceMails.put(id, new VoiceMail(id));
            }
            return voiceMails.get(id);
        }
    }

    private final String id;

    private VoiceMail(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
