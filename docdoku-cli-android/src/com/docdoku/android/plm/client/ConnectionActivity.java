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
import android.app.PendingIntent;
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
import com.docdoku.android.plm.client.documents.DocumentCompleteListActivity;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * <code>Activity</code> that handles the user's input of connection data (if necessary), the verification of the Id provided,
 * and the initialization of the <code>Session</code> data.
 * <p>Layout file: {@link /res/layout/activity_connection.xml activity_connection}
 *
 * @author: Martin Devillers
 */
public class ConnectionActivity extends Activity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.ConnectionActivity";

    /**
     * <code>Intent</code> key for a <code>boolean</code> indicating this <code>Activity</code> was started after the user disconnected from inside the
     * application. The user's data must be erased from memory and no splash screen is shown.
     */
    public static final String INTENT_KEY_ERASE_ID = "erase_id";
    /**
     * <code>Intent</code> key for a <code>Parcelable</code> which is a <code>PendingIntent</code> to start an activity (other than the default one) after the
     * connection process. Used when the connection is started following a click on a <code>Notification</code>.
     * @see PendingIntent
     */
    public static final String INTENT_KEY_PENDING_INTENT = "pending intent";

    private Session session;
    private ProgressDialog progressDialog;
    private AsyncTask connectionTask;
    private CheckBox rememberId;
    private boolean autoConnect;
    private PendingIntent pendingIntent;

    /**
     * Called on the <code>Activity</code>'s creation.
     *
     * <p>- Sets the auto connect <code>Checkbox</code> to checked by default.
     * <p>- Extracts the data from the <code>Intent</code>. If it indicates that data should be erased, calls {@link #eraseData() eraseData()}.
     * <p>- Calls {@link #startConnection() startConnection()} to set the <code>OnClickListener</code> on the connection button.
     * <p>- Checks if the <code>Session</code> data is available in the <Code>SharedPreferences</Code>. If it is, attempts to connect with this data.
     * <p>- If this activity was not started from inside the application, start the splash screen activity: {@link WelcomeScreen}.
     *
     * @param savedInstanceState
     *
     * @see Activity
     */
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
        pendingIntent = intent.getParcelableExtra(INTENT_KEY_PENDING_INTENT);

        startConnection();
        if (Session.loadSession(this)){
            try {
                autoConnect = true;
                session = Session.getSession();
                connect(session);
            } catch (Session.SessionLoadException e) {
                Log.e(LOG_TAG, "Error: session incorrectly loaded");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        if (!eraseData){
            Intent welcomeIntent = new Intent(this, WelcomeScreen.class);
            startActivity(welcomeIntent);
        }
    }

    /**
     * Back button disabled
     *
     * @see Activity
     */
    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    /**
     * Verifies the Id contained in the <code>Session</code> by attempting to connect to the server to download the workspaces.
     *
     * <p>Checks that the internet connection is available.
     * <p> - If it isn't, an <code>AlertDialog</code> is shown to indicate it to the user.
     * <p> - If it is, the a <code>Progressdialog</code> is shown to indicate that the connection process is taking place, and once
     * it is visible, the connection begins by starting a new {@link HttpGetTask}.
     *
     * @param session the session object containing the connection data
     */
    private void connect(final Session session){
        if (checkInternetConnection()){
            Log.i(LOG_TAG, "Showing progress dialog");
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
                    Log.i(LOG_TAG, "Progress dialog shown");
                    try {
                        Log.i(LOG_TAG, "Attempting to connect to server for identification");
                        connectionTask = new HttpGetTask(session, ConnectionActivity.this).execute("/api/accounts/workspaces");
                    } catch (UnsupportedEncodingException e) {
                        Log.e(LOG_TAG,"Error encoding id for server connection");
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            });
            progressDialog.show();
        } else {
            Log.i(LOG_TAG, "No internet connection available");
            new AlertDialog.Builder(this)
                .setMessage(R.string.noConnectionAvailable)
                .setNegativeButton(R.string.OK, null)
                .create().show();
        }
    }

    /**
     * Checks that the device is connected to the internet.
     *
     * @return whether the device's internet connection is available or not
     *
     * @see ConnectivityManager
     */
    private boolean checkInternetConnection(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null){
            Log.i(LOG_TAG, "Connected to network with type code: " + info.getType());
            return info.isConnected();
        }
        else{
            Log.i(LOG_TAG, "Not connected to any data network");
            return false;
        }
    }

    /**
     * Sets the <code>OnClickListener</code> for the connection <code>Button</code>. The <code>onClick()</code> method reads the value of the id input field to
     * create a new {@link Session Session} and start calls {@link #connect(Session) connect()}.
     */
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

    /**
     * If this activity was provided with a <code>PendingIntent</code>, starts this intent. Otherwise, starts the default
     * activity, which is {@link DocumentCompleteListActivity}. After starting this <code>Intent</code>, this <code>Activity</code> is finished.
     *
     * @see PendingIntent
     */
    private void endConnectionActivity(){
        if (pendingIntent == null){
            Intent documentListIntent = new Intent(this, DocumentCompleteListActivity.class);
            startActivity(documentListIntent);
        }else{
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Intent documentListIntent = new Intent(this, DocumentCompleteListActivity.class);
                startActivity(documentListIntent);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        finish();
    }

    /**
     * Erases all data contained in this application package's <code>SharedPreferences</code>.
     *
     * @see SharedPreferences
     */
    void eraseData(){
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

    /**
     * Handles the result of the request to the server.
     *
     * <p>If the request failed, the result is a message indicating the error type.
     * An <code>AlertDialog</code> is shown to indicate the user of this error.
     * <p>If the request was successful, the result is a <code>JSONArray</code> of the workspaces of which the user is a member.
     * These workspaces are set in the <code>Session</code>. A new (asynchronous) request is started to load the user's name,
     * to be presented instead of his login once it is obtained. If the auto connect <code>Checkbox</code> was checked,
     * a {@link GCMIntentService} is started to register for GCM messaging. The {@link #endConnectionActivity() endConnectionActivity()} method is called.
     *
     * @param result
     *
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
    @Override
    public void onHttpGetResult(String result) {
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss(); Log.i(LOG_TAG, "Dismissing connection dialog");
        }else{
            Log.i(LOG_TAG, "Connection dialog not showing on request result");
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
                    Log.i(LOG_TAG, "Workspace downloaded: " + workspaceJSON.getJSONObject(i).getString("id"));
                }
                session.setDownloadedWorkspaces(this, workspaceArray);
                new HttpGetTask(new HttpGetTask.HttpGetListener() {
                    @Override
                    public void onHttpGetResult(String result) {
                        try {
                            JSONObject userJSON = new JSONObject(result);
                            session.setUserName(userJSON.getString("name"));
                        } catch (JSONException e) {
                            Log.w(LOG_TAG, "Unable to read json containing current user's name");
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }).execute("/api/accounts/me");
                if (autoConnect){
                    Intent GCMRegisterIntent = new Intent(this, GCMRegisterService.class);
                    GCMRegisterIntent.putExtra(GCMRegisterService.INTENT_KEY_ACTION, GCMRegisterService.ACTION_SEND_ID);
                    startService(GCMRegisterIntent);
                }
            }catch (JSONException e) {
                Log.e(LOG_TAG,"Error creating workspace JSONArray from String result");
                e.printStackTrace();
            }
            endConnectionActivity();
        }
    }

    /**
     * Creates an <code>AlertDialog</code> with the error message indicated by the resource Id provided.
     *
     * @param messageId The id of the resource containing the error message
     */
    private void createErrorDialog(int messageId){
        new AlertDialog.Builder(this)
            .setMessage(messageId)
            .setNegativeButton(getResources().getString(R.string.OK), null)
            .create().show();
    }
}