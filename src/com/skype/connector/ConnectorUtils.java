/*******************************************************************************
 * Copyright (c) 2006-2007 Koji Hisano <hisano@gmail.com> - UBION Inc. Developer
 * Copyright (c) 2006-2007 UBION Inc. <http://www.ubion.co.jp/>
 * 
 * Copyright (c) 2006-2007 Skype Technologies S.A. <http://www.skype.com/>
 * 
 * Skype4Java is licensed under either the Apache License, Version 2.0 or
 * the Eclipse Public License v1.0.
 * You may use it freely in commercial and non-commercial products.
 * You may obtain a copy of the licenses at
 *
 *   the Apache License - http://www.apache.org/licenses/LICENSE-2.0
 *   the Eclipse Public License - http://www.eclipse.org/legal/epl-v10.html
 *
 * If it is possible to cooperate with the publicity of Skype4Java, please add
 * links to the Skype4Java web site <https://developer.skype.com/wiki/Java_API> 
 * in your web site or documents.
 * 
 * Contributors:
 * Koji Hisano - initial API and implementation
 * Bart Lamot - ExtractFormJar methods.
 ******************************************************************************/
package com.skype.connector;


/**
 * Connector helper class.
 * Generic helper methods for all connectors.
 */
public final class ConnectorUtils {
	/**
	 * Check an object if its not null.
	 * If it is a NullPointerException will be thrown.
	 * @param name Name of the object, used in the exception message.
	 * @param value The object to check.
	 */
	public static void checkNotNull(String name, Object value) {
        if (value == null) {
            throw new NullPointerException("The " + name + " must not be null.");
        }
    }
    
	/**
	 * The methods of this class should be used staticly.
	 * That is why the constructor is private.
	 */
    private ConnectorUtils() {
    }
}
