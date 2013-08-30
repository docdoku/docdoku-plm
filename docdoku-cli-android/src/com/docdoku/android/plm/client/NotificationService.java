package com.docdoku.android.plm.client;

import android.app.PendingIntent;
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
    private static final String LOG_TAG = "com.docdoku.android.plm.client.NotificationService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int j){
        Log.i(LOG_TAG, "Click on notification detected. Starting NotificationService.");
        Bundle bundle = intent.getExtras();
        String docRef = bundle.getString("docReference");
        String workspaceId = bundle.getString("workspaceId");

        try {
            Session session = Session.getSession(this);
            session.setCurrentWorkspace(this, workspaceId);
            new HttpGetTask(session, this).execute("/api/workspaces/" + workspaceId + "/documents/" + docRef);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "UnsupportedEncodingException in NotificationService");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Session.SessionLoadException e) {
            Log.e(LOG_TAG, "Failed to load session to start application from notification");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onHttpGetResult(String result) {
        Log.i(LOG_TAG, "Downloaded document that caused a notification");
        try {
            JSONObject documentJson = new JSONObject(result);
            Document document = new Document(documentJson.getString("id"));
            document.updateFromJSON(documentJson, getResources());
            Intent documentIntent = new Intent(this, DocumentActivity.class);
            documentIntent.putExtra(DocumentActivity.EXTRA_DOCUMENT, document);
            PendingIntent pendingIntent =  PendingIntent.getActivity(this, 0, documentIntent, 0);
            Intent intent = new Intent(this, ConnectionActivity.class);
            intent.putExtra(ConnectionActivity.INTENT_KEY_PENDING_INTENT, pendingIntent);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopSelf();
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
