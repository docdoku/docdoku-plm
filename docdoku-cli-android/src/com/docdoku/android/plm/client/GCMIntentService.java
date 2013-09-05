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

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Class called by {@link GCMBroadcastReceiver} when a GCM message is received. Handles the message and creates a <code>Notification</code>.
 *
 * @author: martindevillers
 */
public class GCMIntentService extends IntentService {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.GCMIntentService";

    private static final long VIBRATION_DURATION_MILLIS = 800;

    private static final String INTENT_KEY_DOCUMENT_ID = "documentMasterId";
    private static final String INTENT_KEY_DOCUMENT_VERSION = "documentMasterVersion";
    private static final String INTENT_KEY_DOCUMENT_ITERATION = "documentMasterIteration";
    private static final String INTENT_KEY_NOTIFICATION_TYPE = "type";
    private static final String INTENT_KEY_WORKSPACE_ID = "workspaceId";
    private static final String INTENT_KEY_DOCUMENT_HASHCODE = "hashCode";

    public GCMIntentService() {
        super("GcmIntentService");
    }

    /**
     * Called when this <code>Service</code> is started.
     * <p>Extracts the data from the message, then calls {@link #sendNotification} to show a <code>Notification</code> to the user.
     * The <code>Document</code>'s hashcode (on the server) is used as an id for the message, so that only one notification per document
     * can be shown simultaneously.
     *
     * @param intent The intent that started this service, containing the GCM message.
     */
    @Override
    protected void onHandleIntent(Intent intent){
        Bundle bundle = intent.getExtras();

        /*
        //Displays all the data received in the GCM message
        Log.i(LOG_TAG, "Printing out all extras in the intent:");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.i(LOG_TAG, String.format("Key: %s;  Value: %s; Type (%s)", key,
                    value.toString(), value.getClass().getName()));
        }
        */

        Log.i(LOG_TAG, "Received GCM message indicating a new iteration/state change");
        String docId = bundle.getString(INTENT_KEY_DOCUMENT_ID);
        String docVersion = bundle.getString(INTENT_KEY_DOCUMENT_VERSION);
        String docIteration = bundle.getString(INTENT_KEY_DOCUMENT_ITERATION);
        String notificationType = bundle.getString(INTENT_KEY_NOTIFICATION_TYPE);
        String workspaceId = bundle.getString(INTENT_KEY_WORKSPACE_ID);
        String docHashCode = bundle.getString(INTENT_KEY_DOCUMENT_HASHCODE);
        int notificationCode;
        try{
            notificationCode = Integer.parseInt(docHashCode);
        }catch(NumberFormatException e){
            Log.e(LOG_TAG, "Received hashcode for document was not an integer. Value: " + docHashCode);
            notificationCode = (int) (Math.random()*1000000);
        }

        sendNotification(docId + "-" + docVersion, docIteration, notificationType, workspaceId, notificationCode);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Show a <code>Notification</code> to the user for the document.
     * <p>Sets the <code>PendingIntent</code> for the notification, which will start a {@link NotificationService}
     * with this document's id and workspaceId as extra.
     * <p>Makes the phone vibrate
     *
     * @param docReference the reference of the document
     * @param iterationNumber the new iteration number
     * @param notificationType the notification type (state change or new iteration)
     * @param workspaceId the id of the workspace, to
     * @param notificationCode
     */
    private void sendNotification(String docReference, String iterationNumber, String notificationType, String workspaceId, int notificationCode){
        Log.i(LOG_TAG, "Showing notification for document " +
                "\nNotification type: " + notificationType +
                "\nDocument reference: " + docReference +
                "\nDocument iteration" + iterationNumber +
                "\nNotification code: " + notificationCode +
                "\nWorkspace id: " + workspaceId);
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra("docReference", docReference);
        notificationIntent.putExtra("workspaceId", workspaceId);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_notifications)
                .setContentTitle(docReference)
                .setContentText(formatNotificationType(notificationType, iterationNumber))
                .setVibrate(new long[]{0, VIBRATION_DURATION_MILLIS})
                .setContentIntent(pendingIntent)
                .build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(notificationCode, notification);
        stopSelf();
    }

    /**
     * Converts the string read from the GCM message into a readable message indicating the notification type.
     * If it is an iteration notification, then the new iteration number is included in the message.
     *
     * @param notificationType the notification type read from the GCM message
     * @param iterationNumber the new iteration number
     * @return The <code>String</code> to be presented to the user to indicate the notification type
     */
    private String formatNotificationType(String notificationType, String iterationNumber){
        if ("iterationNotification".equals(notificationType)){
            return getResources().getString(R.string.documentIterationNotified) + ": " + iterationNumber;
        }else if ("stateNotification".equals(notificationType)){
            return getResources().getString(R.string.documentStateChangeNotified);
        }
        else return "";
    }
}
