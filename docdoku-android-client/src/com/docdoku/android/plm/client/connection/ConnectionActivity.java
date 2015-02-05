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
import com.docdoku.android.plm.client.GCM.GCMRegisterService;
import com.docdoku.android.plm.client.R;
import com.docdoku.android.plm.client.Session;
import com.docdoku.android.plm.client.documents.DocumentCompleteListActivity;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * {@code Activity} that authenticates the user with the DocDokuPLM server.
 * <p>
 * If the user has not enabled the automatic connection, then this {@code Activity} will present him with the {@code EditText} fields to
 * enter the server url, his login, and his password. He may also enable the automatic connection. The connection
 * {@code Button} is then pressed to start the connection process.
 * <br>If the user has previously enabled the automatic connection, then his data is automatically loaded from memory and
 * the connection process is started.
 * <p>
 * The connection process uses a {@link Session} instance which is created with the data available in memory or given by
 * the user to store the connection information. {@link #connect()} is
 * then called to begin the connection process.
 * <p>
 * The connection process consists in the following steps:
 * <br>1. An {@code AlerDialog} is shown to the user to indicate to him that the connection is taking place.
 * <br>2. Once this {@code AlertDialog} becomes visible, an Http request is sent to the server to download the list of
 * workspaces to which he has access. The credentials provided by the user are used to execute this request.
 * <br>3.a. The request response is received, containing a {@code JSONArray}. The authentication
 * process was successful, and the workspaces contained in the {@code JSONArray} are passed to the user's {@code Session}.
 * A new task is then started to query the server for the user's real name which, once it is obtained, will be used
 * to display to the user instead of his login, for a more pleasant user experience. {@link #endConnectionActivity()} is
 * called to start the default {@code Activity}.
 * <br>3.b The response is not a {@code JSONArray}. In that case, it is an error message indicating the server connection
 * error type. This error is used to display an {@code AlertDialog} to the user indicating that the connection has failed.
 * <p>
 * Layout file: {@link /res/layout/activity_connection.xml activity_connection}
 *
 * @author: Martin Devillers
 * @version 1.0
 */
public class ConnectionActivity extends Activity implements HttpGetTask.HttpGetListener {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.connection.ConnectionActivity";

    /**
     * {@code Intent Extra} key for a {@code boolean} indicating that this {@code Activity} was started after the user disconnected from inside the
     * application. The user's data must be erased from memory and the {@link WelcomeScreen} is not shown.
     */
    public static final String INTENT_KEY_ERASE_ID = "erase_id";
    /**
     * {@code Intent Extra} key for a {@code Parcelable} which is a {@code PendingIntent} to start an {@code Activity} (other than the default one) after the
     * connection process. This is used when the connection is started from {@link com.docdoku.android.plm.client.GCM.NotificationService},
     * following a click on a {@code Notification}.
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
     * Called on the {@code Activity}'s creation.
     * <p>
     * - Extracts the data from the {@code Intent}. If it indicates that data should be erased, calls {@link #eraseData()}.
     * <br>- Calls {@link #startConnection()} to set the {@code OnClickListener} on the connection button that starts
     * the connection process.
     * <br>- Calls {@link Session#loadSession(android.content.Context) Seesion.loadSession()} to check if
     * the {@code SharedPreferences} contain data for an automatic connection. If that data if found, attempts to connect
     * with this it.
     * <br>- If this activity was not started due to a disconnection from inside the application, start the splash
     * screen {@code Activity}: {@link WelcomeScreen}.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connection);

        rememberId = (CheckBox) findViewById(R.id.rememberID);

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
                connect();
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
     * Called when the user presses the {@code Android} back button.
     * <p>
     * The back button is disabled in this {@code Activity}, so this method does nothing.
     */
    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    /**
     * Verifies the Id contained in the {@link Session} by attempting to connect to the server to download the workspaces.
     *
     * <p>
     * Checks that the internet connection is available.
     * <br> - If it isn't, an {@code AlertDialog} is shown to indicate it to the user.
     * <br> - If it is, the a {@code ProgressDialog} (with indeterminate progress) is shown to indicate that the
     * connection process is taking place. Once it is visible, the connection begins by calling
     * {@link #downloadWorkspaces()}.
     *
     */
    private void connect(){
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
                    downloadWorkspaces();
                }
            });
            progressDialog.show();
        } else {
            Log.i(LOG_TAG, "No internet connection available");
            createErrorDialog(R.string.noConnectionAvailable);
        }
    }

    /**
     * Attempts to download workspaces from server.
     * <p>
     * Starts a new {@link HttpGetTask} to download the workspaces, with this {@code ConnectionActivity} set as the
     * {@link com.docdoku.android.plm.network.HttpGetTask.HttpGetListener}.
     */
    private void downloadWorkspaces(){
        try {
            Log.i(LOG_TAG, "Attempting to download workspaces from server");
            connectionTask = new HttpGetTask(session, ConnectionActivity.this).execute("/api/accounts/workspaces");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG,"Error encoding id for server connection");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Checks that the device is connected to the internet.
     * <p>
     * The network type code is printed into the {@code Log}.
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
     * Sets the {@code OnClickListener} for the connection {@code Button}.
     * <p>
     * The {@code onClick()} method reads the value
     * of the id input fields to create a new {@link Session Session} and then calls {@link #connect()}.
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
                connect();
            }
        });
    }

    /**
     * Finishes this {@code Activity}, starting a new one.
     * <p>
     * If this activity was provided with a {@code PendingIntent}, starts a new {@code Activity} using it. Otherwise, starts the default
     * activity, which is {@link DocumentCompleteListActivity}. After starting the {@code Intent}, this
     * {@code Activity} is finished.
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
     * Erases all data contained in this application package's {@code SharedPreferences}.
     * <p>
     * This method not only erases the user's login data, but also all of his navigation history. It opens the
     * directory containing all of the application's {@code Preferences} and erases all the files found there.
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
     * <p>
     * If the request failed, the result is a message indicating the error type. An {@code AlertDialog} is shown to
     * notify the user of this error by calling {@link #createErrorDialog(int) createErrorDialog()}. If the error is with the authentication or with the server's url, this is
     * specified. Otherwise, a default connection error message is specified.
     * <p>If the request was successful, the result is a {@code JSONArray} of the workspaces of which the user is a member.
     * This array of workspaces is transferred to the {@link Session} so that it can be stored in memory and in the
     * {@code Preferences}. A new {@link HttpGetTask} is started to load request the server for the user's name,
     * to be presented instead of his login once it is obtained. If the auto connect {@code CheckBox} was checked,
     * a {@link com.docdoku.android.plm.client.GCM.GCMIntentService} is started to register for GCM messaging.
     * The {@link #endConnectionActivity()} method is called.
     *
     * @param result the result of the {@code HttpGetTask} started by {@link #connect()}
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
     * Creates an {@code AlertDialog} with the error message indicated by the resource Id provided.
     *
     * @param messageId The id of the resource containing the error message
     */
    private void createErrorDialog(int messageId){
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.error_light)
            .setMessage(messageId)
            .setNegativeButton(getResources().getString(R.string.OK), null)
            .create().show();
    }
}