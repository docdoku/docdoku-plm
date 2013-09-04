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

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
 * <code>Service</code> called when a user clicks on a <code>Notification</code>. Attempts to start a {@link DocumentActivity}
 * for the {@link Document} whose Id was indicated in the <code>Notification</code>.
 *
 * @author: martindevillers
 */
public class NotificationService extends Service implements HttpGetTask.HttpGetListener{
    private static final String LOG_TAG = "com.docdoku.android.plm.client.NotificationService";

    /**
     * Unused method. Useful if this <code>Service</code> was bound to an <code>Activity</code>, which it shouldn't be.
     *
     * @param intent
     * @return
     * @see Service
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called when this <code>Service</code> is started.
     * <p>Loads the {@link Session} information in order to be able to send a request to the server.
     * <p>Extracts the <code>Document</code> data from the <code>Extras</code> in the <code>Intent</code> to start a {@link HttpGetTask} to fetch
     * the information about the document from the server.
     *
     * @param intent the <code>PendingIntent</code> created in the {@link GCMIntentService}
     * @param i
     * @param j
     * @return
     * @see Service
     */
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

    /**
     * Handles the result of the {@link HttpGetTask}.
     * <p>If the Http request was successful, the result is a <code>JSONObject</code> representing the <code>Document</code>.
     * A <code>PendingIntent</code> is created to start the {@link DocumentActivity} for this {@link Document} and it is
     * put inside of another <code>Intent</code> which starts the {@link ConnectionActivity};
     *
     * @param result
     * @see com.docdoku.android.plm.network.HttpGetTask.HttpGetListener
     */
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
