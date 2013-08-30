package com.docdoku.android.plm.client;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author: martindevillers
 */
public class GCMIntentService extends IntentService {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.GCMIntentService";

    private static final long VIBRATION_DURATION_MILLIS = 800;

    private static final String INTENT_KEY_DOCUMENT_ID = "documentMasterId";
    private static final String INTENT_KEY_DOCUMENT_VERSION = "documentMasterVersion";
    private static final String INTENT_KEY_NOTIFICATION_TYPE = "type";
    private static final String INTENT_KEY_WORKSPACE_ID = "workspaceId";
    private static final String INTENT_KEY_DOCUMENT_HASHCODE = "hashCode";

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Bundle bundle = intent.getExtras();

        /*Log.i(LOG_TAG, "Printing out all extras in the intent:");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.i(LOG_TAG, String.format("Key: %s;  Value: %s; Type (%s)", key,
                    value.toString(), value.getClass().getName()));
        }*/

        Log.i(LOG_TAG, "Received GCM message indicating a new iteration/state change");
        String docId = bundle.getString(INTENT_KEY_DOCUMENT_ID);
        String docVersion = bundle.getString(INTENT_KEY_DOCUMENT_VERSION);
        String notificationType = bundle.getString(INTENT_KEY_NOTIFICATION_TYPE);
        String workspaceId = bundle.getString(INTENT_KEY_WORKSPACE_ID);
        String docHashCode = bundle.getString(INTENT_KEY_DOCUMENT_HASHCODE);
        int notificationCode = 0;
        try{
            notificationCode = Integer.parseInt(docHashCode);
        }catch(NumberFormatException e){
            Log.e(LOG_TAG, "Received hashcode for document was not an integer. Value: " + docHashCode);
            notificationCode = (int) (Math.random()*1000000);
        }

        sendNotification(docId + "-" + docVersion, notificationType, workspaceId, notificationCode);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String docReference, String notificationType, String workspaceId, int notificationCode){
        Log.i(LOG_TAG, "Showing notification for document " +
                "\nNotification type: " + notificationType +
                "\nDocument reference: " + docReference +
                "\nNotification code: " + notificationCode +
                "\nWorkspace id: " + workspaceId);
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.putExtra("docReference", docReference);
        notificationIntent.putExtra("workspaceId", workspaceId);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_notifications)
                .setContentTitle(docReference)
                .setContentText(formatNotificationType(notificationType))
                .setVibrate(new long[]{0, VIBRATION_DURATION_MILLIS})
                .setContentIntent(pendingIntent)
                .build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(notificationCode, notification);
        stopSelf();
    }

    private String formatNotificationType(String notificationType){
        if ("iterationNotification".equals(notificationType)){
            return getResources().getString(R.string.documentIterationNotified);
        }else if ("stateNotification".equals(notificationType)){
            return getResources().getString(R.string.documentStateChangeNotified);
        }
        else return "";
    }
}
