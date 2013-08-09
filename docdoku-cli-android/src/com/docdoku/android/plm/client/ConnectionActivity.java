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

package com.docdoku.android.plm.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.docdoku.android.plm.network.listeners.HttpGetListener;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author: Martin Devillers
 */
public class ConnectionActivity extends Activity implements HttpGetListener {

    private static final String PREFERENCE_USERNAME = "username";
    private static final String PREFERENCE_PASSWORD = "password";
    private static final String PREFERENCE_SERVER_URL = "server url";
    private static final String PREFERENCE_AUTO_CONNECT = "auto_connect";
    public static final String INTENT_ERASE_ID = "erase_id";

    private CheckBox rememberId;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private String username, password, serverUrl;
    private AsyncTask connectionTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connection);

        rememberId = (CheckBox) findViewById(R.id.rememberID);
        preferences = getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
        boolean eraseData = intent.getBooleanExtra(INTENT_ERASE_ID, false);
        if (eraseData){
            eraseData();
        }

        startConnection();
        if (preferences.getBoolean(PREFERENCE_AUTO_CONNECT, false)){
            username = preferences.getString(PREFERENCE_USERNAME, "");
            password = preferences.getString(PREFERENCE_PASSWORD, "");
            serverUrl = preferences.getString(PREFERENCE_SERVER_URL, "");

            connect(username, password, serverUrl);
        }
    }

    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    private void connect(final String username, final String password, String serverUrl){
        if (checkInternetConnection()){
            final String checkedUrl = checkUrlFormat(serverUrl);
            Log.i("com.docdoku.android.plm.client", "Showing progress dialog");
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getResources().getString(R.string.connectingToServer));
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (connectionTask != null){
                        connectionTask.cancel(true);
                    }
                }
            });
            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Log.i("com.docdoku.android.plm.client", "Progress dialog shown");
                    try {
                        Log.i("com.docdoku.android.plm.client", "Attempting to connect to server for identification");
                        connectionTask = new HttpGetTask(checkedUrl, username, password, ConnectionActivity.this).execute("api/accounts/workspaces");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("com.docdoku.android.plm.client","Error encoding id for server connection");
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });
            progressDialog.show();
        } else {
            Log.i("com.docdoku.android.plm.client", "No internet connection available");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.noConnectionAvailable);
            builder.setNegativeButton(R.string.OK, null);
            builder.create().show();
        }
    }

    private boolean checkInternetConnection(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null){
            Log.i("com.docdoku.android.plm.client", "Connected to network with type code: " + info.getType());
            return info.isConnected();
        }
        else{
            Log.i("com.docdoku.android.plm.client", "Not connected to any data network");
            return false;
        }
    }

    private void startConnection(){
        Button connection = (Button) findViewById(R.id.connection);
        connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = ((EditText) findViewById(R.id.usernameField)).getText().toString();
                password = ((EditText) findViewById(R.id.passwordField)).getText().toString();
                serverUrl = ((EditText) findViewById(R.id.urlField)).getText().toString();
                connect(username,password,serverUrl);
            }
        });
    }

    private void endConnectionActivity(){
        Intent intent = new Intent(ConnectionActivity.this, DocumentCompleteListActivity.class);
        startActivity(intent);
        finish();
    }

    private String checkUrlFormat(String url){
        String finalUrl = url;
        if (url.length()>0 && !(url.substring(url.length()-1).equals("/"))){
            finalUrl += "/";
        }
        return finalUrl;
    }

    public void eraseData(){
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @Override
    public void onHttpGetResult(String result) {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss(); Log.i("com.docdoku.android.plm.client", "Dismissing connection dialog");
        }else{
            Log.i("com.docdoku.android.plm.client", "Connection dialog not showing on request result");
        }
        if(result == null || result.equals(HttpGetTask.ERROR_UNKNOWN) || result.equals(HttpGetTask.ERROR_HTTP_BAD_REQUEST)){
            createErrorDialog(R.string.connectionError);
        }else if (result.equals(HttpGetTask.ERROR_HTTP_UNAUTHORIZED)){
            createErrorDialog(R.string.wrongUsernamePassword);
        }else if (result.equals(HttpGetTask.ERROR_URL)){
            createErrorDialog(R.string.serverUrlError);
        }else{
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
                MenuFragment.setDOWNLOADED_WORKSPACES(workspaceArray);
            }catch (JSONException e) {
                Log.e("com.docdoku.android.plm.client","Error creating workspace JSONArray from String result");
                e.printStackTrace();
            }
            if (rememberId.isChecked()){
                Log.i("com.docdoku.android.plm.client", "Saving in memory user identification for: " + username);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCE_AUTO_CONNECT, true);
                editor.putString(PREFERENCE_USERNAME, username);
                editor.putString(PREFERENCE_PASSWORD, password);
                editor.putString(PREFERENCE_SERVER_URL, serverUrl);
                editor.commit();
            }
            endConnectionActivity();
        }
    }

    private void createErrorDialog(int messageId){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageId);
        builder.setNegativeButton(getResources().getString(R.string.OK), null);
        builder.create().show();
    }
}