/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.android.plm.client.connection;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import com.docdoku.android.plm.client.R;

/**
 * Displays a welcome screen
 *<p>Layout file: {@link /res/layout/welcome_screen.xml welcome_screen}
 *
 * @author: martindevillers
 */
public class WelcomeScreen extends Activity{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.connection.WelcomeScreen";

    private static final long WELCOME_SCREEN_DURATION_MILLIS = 3000;

    /**
     * Inflates the layout
     *
     * @param savedInstanceState
     * @see Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_screen);
    }

    /**
     * Wait for <code>WELCOME_SCREEN_DURATION_MILLIS</code> milliseconds then calls {@link #finish()} to end this <code>Activity</code>
     *
     * @see Activity
     */
    @Override
    public void onResume(){
        Log.i(LOG_TAG, "Showing Splash screen");
        super.onResume();
        new Thread(){
            @Override
            public void run(){
                synchronized (this){
                    try {
                        wait(WELCOME_SCREEN_DURATION_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    endActivity();
                }
            }
        }.start();
    }

    /**
     * Back button disabled
     */
    @Override
    public void onBackPressed(){
        //Back button disabled
    }

    /**
     * Finishes this <code>Activity</code>
     */
    private void endActivity(){
        finish();
    }
}