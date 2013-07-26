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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author: Martin Devillers
 */
public class ConnectionActivity extends Activity implements HttpGetListener {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SERVER_URL = "server url";
    private static final String AUTO_CONNECT = "auto_connect";
    public static final String ERASE_ID = "erase_id";

    private CheckBox rememberId;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private String username, password, serverUrl;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connection);

        rememberId = (CheckBox) findViewById(R.id.rememberID);
        preferences = getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
        boolean eraseID = intent.getBooleanExtra(ERASE_ID, false);
        if (eraseID){
            eraseID();
        }

        startConnection();
        if (preferences.getBoolean(AUTO_CONNECT, false)){
            username = preferences.getString(USERNAME, "");
            password = preferences.getString(PASSWORD, "");
            serverUrl = preferences.getString(SERVER_URL, "");

            connect(username, password, serverUrl);
        }
    }

    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    private void connect(String username, String password, String serverUrl){
        if (checkInternetConnection()){
            try {
                String checkedUrl = checkUrlFormat(serverUrl);
                Log.i("docDoku.DocDokuPLM", "Attempting to connect to server for identification");
                Log.i("docDoku.DocDokuPLM", "Showing progress dialog");
                progressDialog = ProgressDialog.show(this, "Connecting to server", "", false, false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                new HttpGetTask(checkedUrl,username,password,this).execute("api/accounts/workspaces");
            } catch (UnsupportedEncodingException e) {
                Log.e("docDoku.DocDokuPLM","Error encoding id for server connection");
                e.printStackTrace();
            }
        }
        else {
            Log.i("docDoku.DocDokuPLM", "No internet connection available");
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
            Log.i("docDoku.DocDokuPLM", "Connected to network with type code: " + info.getType());
            return info.isConnected();
        }
        else{
            Log.i("docDoku.DocDokuPLM", "Not connected to any data network");
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
        Intent intent = new Intent(ConnectionActivity.this, DocumentListActivity.class);
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

    public void eraseID(){
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AUTO_CONNECT, false);
        editor.putString(USERNAME, "");
        editor.putString(PASSWORD, "");
        editor.commit();
    }

    @Override
    public void onHttpGetResult(String result) {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss(); Log.i("docDoku.DocDokuPLM", "Dismissing connection dialog");
        }
        else{
            Log.i("docDoku.DocDokuPLM", "Connection dialog not showing");
        }
        if (result.equals(HttpGetTask.CONNECTION_ERROR)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.wrongUsernamePassword));
            builder.setNegativeButton(getResources().getString(R.string.OK), null);
            builder.create().show();
        }
        else if (result.equals(HttpGetTask.URL_ERROR)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.serverUrlError));
            builder.setNegativeButton(getResources().getString(R.string.OK), null);
            builder.create().show();
        }
        else{
            ActionBarActivity.currentUserLogin = username;
            try{
                JSONArray workspaceJSON = new JSONArray(result);
                int numWorkspaces = workspaceJSON.length();
                String[] workspaceArray = new String[numWorkspaces];
                for (int i=0; i<numWorkspaces; i++){
                    workspaceArray[i] = workspaceJSON.getJSONObject(i).getString("id");
                    Log.i("docDoku.DocDokuPLM", "Workspace downloaded: " + workspaceJSON.getJSONObject(i).getString("id"));
                }
                MenuFragment.setWorkspaces(workspaceArray);
            }
            catch (JSONException e) {
                Log.e("docDoku.DocDokuPLM","Error creating JSONArray from String result");
                e.printStackTrace();
            }
            if (rememberId.isChecked()){
                Log.i("docDoku.DocDokuPLM", "Enregistrement des identifiants pour: " + username);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(AUTO_CONNECT, true);
                editor.putString(USERNAME, username);
                editor.putString(PASSWORD, password);
                editor.putString(SERVER_URL, serverUrl);
                editor.commit();
            }
            endConnectionActivity();
        }

    }
}