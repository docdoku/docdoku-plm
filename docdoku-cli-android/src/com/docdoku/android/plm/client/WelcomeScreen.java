package com.docdoku.android.plm.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import com.docdoku.android.plm.client.documents.DocumentCompleteListActivity;
import com.docdoku.android.plm.network.HttpGetTask;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * @author: martindevillers
 */
public class WelcomeScreen extends Activity implements HttpGetListener{

    private String username;
    private String password;
    private String serverUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_screen);

        SharedPreferences preferences = getSharedPreferences(ConnectionActivity.class.getSimpleName(), MODE_PRIVATE);
        if (preferences.getBoolean(ConnectionActivity.PREFERENCE_KEY_AUTO_CONNECT, false)){
            username = preferences.getString(ConnectionActivity.PREFERENCE_KEY_USERNAME, "");
            password = preferences.getString(ConnectionActivity.PREFERENCE_KEY_PASSWORD, "");
            serverUrl = preferences.getString(ConnectionActivity.PREFERENCE_KEY_SERVER_URL, "");

            connect(username, password, serverUrl);
        }else{
            Log.i("com.docdoku.android.plm", "No login history was provided");
            (new WaitingThread(this)).start();
        }
    }

    private void connect(final String username, final String password, String serverUrl){
        final String host = ConnectionActivity.extractHostFromUrl(serverUrl);
        final int port = ConnectionActivity.extractPortFromUrl(serverUrl);
        try {
            new HttpGetTask(host, port, username, password, WelcomeScreen.this).execute("/api/accounts/workspaces");
        } catch (UnsupportedEncodingException e) {
            Log.i("com.docdoku.android.plm", "UnsupportedEncodingException in WelcomeScreen");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            endWelcomeScreenWithConnectionFail();
        }
    }

    @Override
    public void onHttpGetResult(String result) {
        SearchActionBarActivity.currentUserLogin = username;
        SimpleActionBarActivity.currentUserLogin = username;
        try{
            JSONArray workspaceJSON = new JSONArray(result);
            int numWorkspaces = workspaceJSON.length();
            String[] workspaceArray = new String[numWorkspaces];
            for (int i=0; i<numWorkspaces; i++){
                workspaceArray[i] = workspaceJSON.getJSONObject(i).getString("id");
                Log.i("com.docdoku.android.plm.client", "Workspace downloaded: " + workspaceJSON.getJSONObject(i).getString("id"));
            }
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            MenuFragment.setDOWNLOADED_WORKSPACES(workspaceArray, preferences);
        }catch (JSONException e) {
            Log.e("com.docdoku.android.plm.client","Error creating workspace JSONArray from String result");
            endWelcomeScreenWithConnectionFail();
            e.printStackTrace();
        }
        endWelcomeScreenWithConnectionSuccess();
    }

    public void endWelcomeScreenWithConnectionFail(){
        Log.i("com.docdoku.android.plm", "Switching to connection screen to provide login data");
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void endWelcomeScreenWithConnectionSuccess(){
        Log.i("com.docdoku.android.plm", "Connection from login in application history was successful!!!");
        Intent intent = new Intent(this, DocumentCompleteListActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}