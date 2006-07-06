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
