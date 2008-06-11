package com.skype.connector.osx;

import java.util.concurrent.CountDownLatch;

public class SkypeFrameworkTest {
    public static void main(String[] args) throws Exception {
        SkypeFramework.init("Skype4Java");

        System.out.println("isRunning: " + SkypeFramework.isRunning());
        System.out.println("isAvabileable: " + SkypeFramework.isAvailable());
        
        final CountDownLatch latch = new CountDownLatch(1);

        SkypeFramework.addSkypeFrameworkListener(new SkypeFrameworkListener() {
            public void becameUnavailable() {
                System.out.println("becameUnavailable");
            }

            public void becameAvailable() {
                System.out.println("becameAvailable");
            }

            public void attachResponse(int attachResponseCode) {
                System.out.println("attachResponse: " + attachResponseCode);
                latch.countDown();
            }

            public void notificationReceived(String notificationString) {
                System.out.println("notificationReceived: " + notificationString);
            }
        });
        
        SkypeFramework.connect();
        try {
            latch.await();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        SkypeFramework.sendCommand("PROTOCOL 9999");
        SkypeFramework.sendCommand("PING");
        
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
