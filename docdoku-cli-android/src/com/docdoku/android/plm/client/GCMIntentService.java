package com.docdoku.android.plm.client;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * @author: martindevillers
 */
public class GCMIntentService extends IntentService {

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        Bundle bundle = intent.getExtras();

        Log.i("com.docdoku.android.plm", "Printing out all extras in the intent:");
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.i("com.docdoku.android.plm", String.format("Key: %s;  Value: %s; Type (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

        String docId = bundle.getString("documentMasterId");
        String docVersion = bundle.getString("documentMasterVersion");
        String notificationType = bundle.getString("type");
        String workspaceId = bundle.getString("workspaceId");

        sendNotification(docId + "-" + docVersion, notificationType, workspaceId);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String docReference, String notificationType, String workspaceId){
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_notifications)
                .setContentTitle(docReference)
                .setContentText(formatNotificationType(notificationType))
                .setVibrate(new long[]{0, 1000})
                .build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
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
