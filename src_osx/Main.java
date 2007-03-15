/*******************************************************************************
 * Copyright (c) 2006 Bart Lamot <bart.almot@gmail.com> 
 * 
 * Copyright (c) 2006 Skype Technologies S.A. <http://www.skype.com/>
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
 * Bart Lamot - initial API and implementation
 ******************************************************************************/
import com.skype.connector.linux.*;
import com.skype.connector.Connector;
import com.skype.Skype;

class Main {
	public static void main(String args[]) {
		try {
			//Connector.getInstance();
			Skype.setDeamon(true);
			Skype.setDebug(true);
			System.out.println("Skype version: "+Skype.getVersion());
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i=0; i < 60; i++) {
			try {
				Thread.sleep(1000);
				Thread.yield();
			} catch(Exception e) {
			}
		}
	try {
		Connector.getInstance().dispose();
	} catch (Exception e) {}
	System.out.println("Main.main() end");
 	System.exit(0);	
	}
}
