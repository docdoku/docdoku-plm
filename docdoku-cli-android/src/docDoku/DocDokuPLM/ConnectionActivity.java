package docDoku.DocDokuPLM;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public class ConnectionActivity extends Activity implements  ServerConnection{

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String AUTO_CONNECT = "auto_connect";
    public static final String ERASE_ID = "erase_id";

    private CheckBox rememberId;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private String username;
    private String password;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        rememberId = (CheckBox) findViewById(R.id.rememberID);
        preferences = getPreferences(MODE_PRIVATE);

        Intent intent = getIntent();
        boolean eraseID = intent.getBooleanExtra(ERASE_ID, false);
        if (eraseID){
            eraseID();
        }

        if (preferences.getBoolean(AUTO_CONNECT, false)){
            username = preferences.getString(USERNAME, "");
            password = preferences.getString(PASSWORD, "");
            connect(username, password);
        }
        else{
            startConnection();
        }
    }

    @Override
    public void onBackPressed(){
        //BACK BUTTON DISABLED
    }

    private void connect(String username, String password){
        if (checkInternetConnection()){
            try {
                Log.i("docDoku.DocDokuPLM", "Attempting to connect to server for identification");
                Log.i("docDoku.DocDokuPLM", "Showing progress dialog");
                progressDialog = ProgressDialog.show(this, "Connecting to server", "", false, false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                //LocalHost
                //AsyncTask serverIdentification = new HttpGetTask("http://10.0.2.2:8080/",username,password,this).execute("faces/login.xhtml");
                AsyncTask serverIdentification = new HttpGetTask("http://192.168.0.11:8080/api/",username,password,this).execute("accounts/workspaces");
                //Distant host
                //AsyncTask serverIdentification = new HttpGetTask("http://docdokuplm.net/",username,password,this).execute("api/");
                //Log.i("docDoku.DocDokuPLM","Workspace array: " + workspaceArray.toString());
                //AsyncTask getWorkspace = new HttpGetTask(this).execute("/workspaces/workspaceDemo2/folders/");
            } catch (UnsupportedEncodingException e) {
                Log.e("docDoku.DocDokuPLM","Error encoding id for server connection");
                e.printStackTrace();
            }
            //MenuFragment.setWorkspaces(new String[]{"Workspace1", "Workspace2", "Workspace3", "Workspace4", "Workspace5"});
        }
        else {
            Log.i("docDoku.DocDokuPLM", "No internet connection available");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.noConnectionAvailable));
            builder.setNegativeButton(getResources().getString(R.string.OK), null);
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
                connect(username,password);
            }
        });
    }

    private void endConnectionActivity(){
        Intent intent = new Intent(ConnectionActivity.this, DocumentListActivity.class);
        startActivity(intent);
        finish();
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
    public void onConnectionResult(String result) {
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
        else{
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
                editor.commit();
            }
            endConnectionActivity();
        }

    }
}