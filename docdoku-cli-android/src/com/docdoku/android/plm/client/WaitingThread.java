package com.docdoku.android.plm.client;

import com.docdoku.android.plm.client.WelcomeScreen;

/**
 * @author: martindevillers
 */
public class WaitingThread extends Thread {

    WelcomeScreen welcomeScreen;

    public WaitingThread(WelcomeScreen welcomeScreen){
        this.welcomeScreen = welcomeScreen;
    }

    @Override
    public void run() {
        synchronized (this){
            try {
                wait(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        welcomeScreen.endWelcomeScreenWithConnectionFail();
    }
}
