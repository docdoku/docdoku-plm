package com.docdoku.android.plm.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import com.docdoku.android.plm.client.documents.DocumentCompleteListActivity;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * @author: martindevillers
 */
public class WelcomeScreen extends Activity{

    private static final long WELCOME_SCREEN_DURATION_MILLIS = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_screen);
    }

    @Override
    public void onResume(){
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

    @Override
    public void onBackPressed(){
        //Back button disabled
    }

    private void endActivity(){
        finish();
    }
}