package com.docdoku.android.plm.client;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.docdoku.android.plm.client.documents.Document;
import com.docdoku.android.plm.client.documents.DocumentActivity;
import com.docdoku.android.plm.network.HttpGetTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * @author: martindevillers
 */
public class NotificationService extends Service implements HttpGetTask.HttpGetListener{

    private String login;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int j){
        Log.i("com.docdoku.android.plm", "Click on notification detected. Starting NotificationService.");
        Bundle bundle = intent.getExtras();
        String docRef = bundle.getString("docReference");
        String workspaceId = bundle.getString("workspaceId");

        try {
            Session session = Session.getSession(this);
            session.setCurrentWorkspace(this, workspaceId);
            new HttpGetTask(session, this).execute("/api/workspaces/" + workspaceId + "/documents/" + docRef);
        } catch (UnsupportedEncodingException e) {
            Log.e("com.docdoku.android.plm", "UnsupportedEncodingException in NotificationService");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Session.SessionLoadException e) {
            Log.e("com.docdoku.android.plm", "Failed to load session to start application from notification");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onHttpGetResult(String result) {
        Log.i("com.docdoku.android.plm", "Downloaded document that caused a notification");
        try {
            JSONObject documentJson = new JSONObject(result);
            Document document = new Document(documentJson.getString("id"));
            document.updateFromJSON(documentJson, getResources());
            Intent intent = new Intent(this, DocumentActivity.class);
            intent.putExtra(DocumentActivity.EXTRA_DOCUMENT, document);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
