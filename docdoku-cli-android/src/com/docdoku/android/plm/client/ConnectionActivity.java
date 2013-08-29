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
import android.content.pm.PackageManager;
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
import com.docdoku.android.plm.client.documents.DocumentCompleteListActivity;
import com.docdoku.android.plm.network.HttpGetTask;
import com.docdoku.android.plm.network.HttpPutTask;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author: Martin Devillers
 */
public class ConnectionActivity extends Activity implements HttpGetTask.HttpGetListener {

    public static final String PREFERENCES_APPLICATION = "DocDokuPLM";
    public static final String PREFERENCE_KEY_USERNAME = "username";
    public static final String PREFERENCE_KEY_PASSWORD = "password";
    public static final String PREFERENCE_KEY_SERVER_URL = "server url";
    public static final String PREFERENCE_KEY_AUTO_CONNECT = "auto_connect";
    public static final String INTENT_KEY_ERASE_ID = "erase_id";

    private static final String PREFERENCE_KEY_GCM_ID = "gcm id";
    private static final String PREFERENCE_KEY_GCM_REGISTRATION_VERSION = "gcm version";
    private static final String PREFERENCE_KEY_GCM_EXPIRATION_DATE = "gcm expiration";
    private static final String SENDER_ID = "263093437022"; //See Google API Console to set Id
    private static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7; //Default lifespan (7 days) of a reservation until it is considered expired.

    private CheckBox rememberId;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private String username, password, serverUrl;
    private AsyncTask connectionTask;
    private String gcmId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connection);

        rememberId = (CheckBox) findViewById(R.id.rememberID);
        preferences = getSharedPreferences(PREFERENCES_APPLICATION, MODE_PRIVATE);

        getGCMId();

        Intent intent = getIntent();
        boolean eraseData = intent.getBooleanExtra(INTENT_KEY_ERASE_ID, false);
        if (eraseData){
            eraseData();
        }

        startConnection();
        if (preferences.getBoolean(PREFERENCE_KEY_AUTO_CONNECT, false)){
            username = preferences.getString(PREFERENCE_KEY_USERNAME, ""); ((EditText) findViewById(R.id.usernameField)).setText(username);
            password = preferences.getString(PREFERENCE_KEY_PASSWORD, ""); ((EditText) findViewById(R.id.passwordField)).setText(password);
            serverUrl = preferences.getString(PREFERENCE_KEY_SERVER_URL, ""); ((EditText) findViewById(R.id.urlField)).setText(serverUrl);

            connect(username, password, serverUrl);
        }

        if (!eraseData){
            Intent welcomeIntent = new Intent(this, WelcomeScreen.class);
            startActivity(welcomeIntent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    private void getGCMId() {
        gcmId = preferences.getString(PREFERENCE_KEY_GCM_ID, "");
        Log.i("com.docdoku.android.plm", "Looking for gcm Id...");
        if (gcmId.length() > 0){
            try {
                if (isGCMIdExpired() || isGCMIdPreviousVersion()){
                    Log.i("com.docdoku.android.plm", "gcm Id belonged to previoud app version or was expired");
                    getNewGCMId();
                }else{
                    Log.i("com.docdoku.android.plm", "gcm Id found! " + gcmId);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("com.docdoku.android.plm", "Could not get package name");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }else{
            Log.i("com.docdoku.android.plm", "No gcm Id was found in storage");
            getNewGCMId();
        }
    }

    private boolean isGCMIdPreviousVersion() throws PackageManager.NameNotFoundException {
        int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        int registeredVersion = preferences.getInt(PREFERENCE_KEY_GCM_REGISTRATION_VERSION, -1);
        return currentVersion != registeredVersion;
    }

    private boolean isGCMIdExpired(){
        long expirationTime = preferences.getLong(PREFERENCE_KEY_GCM_EXPIRATION_DATE, -1);
        return System.currentTimeMillis() > expirationTime;
    }

    private void getNewGCMId(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... objects) {
                GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(ConnectionActivity.this);
                try {
                    String registrationId = googleCloudMessaging.register(SENDER_ID);
                    Log.i("com.docdoku.android.plm", "gcm Id obtained: " + registrationId);
                    //TODO: Send the gcm Id to server for transmitting
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PREFERENCE_KEY_GCM_ID, registrationId);
                    editor.putInt(PREFERENCE_KEY_GCM_REGISTRATION_VERSION, getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
                    editor.putLong(PREFERENCE_KEY_GCM_EXPIRATION_DATE, System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS);
                    editor.commit();
                } catch (IOException e) {
                    Log.e("com.docdoku.android.plm", "IOException when registering for gcm Id");
                    Log.e("com.docdoku.android.plm", "Exception message: " + e.getMessage());
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("com.docdoku.android.plm", "Exception when trying to retrieve app version corresponding to new gcm Id");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }.execute();
    }

    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    private void connect(final String username, final String password, String serverUrl){
        if (checkInternetConnection()){
            final String host = extractHostFromUrl(serverUrl);
            final int port = extractPortFromUrl(serverUrl);
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
                        connectionTask = new HttpGetTask(host, port, username, password, ConnectionActivity.this).execute("/api/accounts/workspaces");
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

    public static String extractHostFromUrl(String url){
        String finalUrl = url;
        if (finalUrl != null && finalUrl.length()>0){
            if (finalUrl.length()>7 && finalUrl.substring(0,7).equals("http://")){
                finalUrl = finalUrl.substring(7, finalUrl.length());
            }
            int semicolonIndex = finalUrl.indexOf(':');
            if (semicolonIndex != -1){
                finalUrl = finalUrl.substring(0, semicolonIndex);
            }
            if (finalUrl.charAt(finalUrl.length()-1) == '/'){
                finalUrl = finalUrl.substring(0, finalUrl.length()-1);
            }
        }
        return finalUrl;
    }

    public static int extractPortFromUrl(String url){
        String finalUrl = url;
        if (finalUrl != null && finalUrl.length()>0){
            if (finalUrl.length()>7 && finalUrl.substring(0,7).equals("http://")){
                finalUrl = finalUrl.substring(7, finalUrl.length());
            }
            int semicolonIndex = finalUrl.indexOf(':');
            if (semicolonIndex != -1){
                String portString = finalUrl.substring(semicolonIndex+1);
                int port = Integer.parseInt(portString);
                Log.i("com.docdoku.android.plm", "Extracted port from Url: " + port);
                return Integer.parseInt(portString);
            }
        }
        return -1;
    }

    public void eraseData(){
        File dir = new File(getFilesDir().getParent() + "/shared_prefs/");
        String[] children = dir.list();
        for (String child: children) {
            // clear each of the preferences
            getSharedPreferences(child.replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
        }
        // Make sure it has enough time to save all the commited changes
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        for (String child: children) {
            // delete the files
            new File(dir, child).delete();
        }
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

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("gcmId", gcmId);
                new HttpPutTask(null).execute("/api/accounts/gcm", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            SimpleActionBarActivity.currentUserLogin = username;
            try{
                JSONArray workspaceJSON = new JSONArray(result);
                int numWorkspaces = workspaceJSON.length();
                String[] workspaceArray = new String[numWorkspaces];
                for (int i=0; i<numWorkspaces; i++){
                    workspaceArray[i] = workspaceJSON.getJSONObject(i).getString("id");
                    Log.i("com.docdoku.android.plm.client", "Workspace downloaded: " + workspaceJSON.getJSONObject(i).getString("id"));
                }
                MenuFragment.setDOWNLOADED_WORKSPACES(workspaceArray, this);
            }catch (JSONException e) {
                Log.e("com.docdoku.android.plm.client","Error creating workspace JSONArray from String result");
                e.printStackTrace();
            }
            if (rememberId.isChecked()){
                Log.i("com.docdoku.android.plm.client", "Saving in memory user identification for: " + username);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PREFERENCE_KEY_AUTO_CONNECT, true);
                editor.putString(PREFERENCE_KEY_USERNAME, username);
                editor.putString(PREFERENCE_KEY_PASSWORD, password);
                editor.putString(PREFERENCE_KEY_SERVER_URL, serverUrl);
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