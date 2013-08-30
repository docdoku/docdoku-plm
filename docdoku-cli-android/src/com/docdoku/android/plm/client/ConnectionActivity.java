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

    public static final String INTENT_KEY_ERASE_ID = "erase_id";

    private Session session;
    private ProgressDialog progressDialog;
    private AsyncTask connectionTask;
    private CheckBox rememberId;
    private boolean autoConnect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connection);

        rememberId = (CheckBox) findViewById(R.id.rememberID);
        rememberId.setChecked(true);

        Intent intent = getIntent();
        boolean eraseData = intent.getBooleanExtra(INTENT_KEY_ERASE_ID, false);
        if (eraseData){
            eraseData();
        }

        startConnection();
        if (Session.loadSession(this)){
            try {
                autoConnect = true;
                session = Session.getSession();
                connect(session);
            } catch (Session.SessionLoadException e) {
                Log.e("com.docdoku.android.plm", "Error: session incorrectly loaded");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        if (!eraseData){
            Intent welcomeIntent = new Intent(this, WelcomeScreen.class);
            startActivity(welcomeIntent);
        }
    }

    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    private void connect(final Session session){
        if (checkInternetConnection()){
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
                        connectionTask = new HttpGetTask(session, ConnectionActivity.this).execute("/api/accounts/workspaces");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("com.docdoku.android.plm.client","Error encoding id for server connection");
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });
            progressDialog.show();
        } else {
            Log.i("com.docdoku.android.plm.client", "No internet connection available");
            new AlertDialog.Builder(this)
                .setMessage(R.string.noConnectionAvailable)
                .setNegativeButton(R.string.OK, null)
                .create().show();
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
                autoConnect = rememberId.isChecked();
                String username = ((EditText) findViewById(R.id.usernameField)).getText().toString();
                String password = ((EditText) findViewById(R.id.passwordField)).getText().toString();
                String serverUrl = ((EditText) findViewById(R.id.urlField)).getText().toString();
                session = Session.initSession(ConnectionActivity.this, autoConnect, username, username, password, serverUrl);
                connect(session);
            }
        });
    }

    private void endConnectionActivity(){
        Intent intent = new Intent(ConnectionActivity.this, DocumentCompleteListActivity.class);
        startActivity(intent);
        finish();
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
            try{
                JSONArray workspaceJSON = new JSONArray(result);
                int numWorkspaces = workspaceJSON.length();
                String[] workspaceArray = new String[numWorkspaces];
                for (int i=0; i<numWorkspaces; i++){
                    workspaceArray[i] = workspaceJSON.getJSONObject(i).getString("id");
                    Log.i("com.docdoku.android.plm.client", "Workspace downloaded: " + workspaceJSON.getJSONObject(i).getString("id"));
                }
                session.setDownloadedWorkspaces(this, workspaceArray);
                if (autoConnect){
                    Intent GCMRegisterIntent = new Intent(this, GCMRegisterService.class);
                    GCMRegisterIntent.putExtra(GCMRegisterService.INTENT_KEY_ACTION, GCMRegisterService.ACTION_SEND_ID);
                    startService(GCMRegisterIntent);
                }
            }catch (JSONException e) {
                Log.e("com.docdoku.android.plm.client","Error creating workspace JSONArray from String result");
                e.printStackTrace();
            }
            endConnectionActivity();
        }
    }

    private void createErrorDialog(int messageId){
        new AlertDialog.Builder(this)
            .setMessage(messageId)
            .setNegativeButton(getResources().getString(R.string.OK), null)
            .create().show();
    }
}