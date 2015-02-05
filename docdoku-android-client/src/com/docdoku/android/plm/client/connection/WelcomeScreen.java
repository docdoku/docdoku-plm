/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
 * Displays a welcome screen.
 * <p>
 * If the user does not have the automatic connection enabled, this screen will last for
 * {@value #WELCOME_SCREEN_DURATION_MILLIS} milliseconds, then this {@code Activity} will finish and
 * {@link ConnectionActivity} will become visible.
 * <br> If the user has the automatic connection enabled, this screen will also last for
 * {@value #WELCOME_SCREEN_DURATION_MILLIS} milliseconds. However, if during this time the connection process finishes,
 * the starting {@code Activity} will be shown on top of this one. If not, then this {@code Activity} will finish and
 * the {@code ConnectionActivity} will become visible with the connection {@code AlertDialog} showing.
 * <p>
 * Layout file: {@link /res/layout/welcome_screen.xml welcome_screen}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class WelcomeScreen extends Activity{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.connection.WelcomeScreen";

    /**
     * Duration that this screen should be shown.
     */
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
     * Called when the graphical layout becomes visible to the user.
     * Starts a {@code Thread} which waits for {@code WELCOME_SCREEN_DURATION_MILLIS} milliseconds then calls
     * {@link #endActivity()} ()} to end this {@code Activity}.
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
     * Called when the user presses the {@code Android} back button.
     * <p>
     * The back button is disabled in this {@code Activity}, so this method does nothing.
     */
    @Override
    public void onBackPressed(){
        //Back button disabled
    }

    /**
     * Finishes this {@code Activity}
     */
    private void endActivity(){
        finish();
    }
}