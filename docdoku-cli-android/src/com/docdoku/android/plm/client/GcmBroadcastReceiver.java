package com.docdoku.android.plm.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * @author: martindevillers
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        String deliveryString=intent.getExtras().getString("message");
        deliveryString= deliveryString.replace("[", "");
        deliveryString= deliveryString.replace("]", "");
        Log.i("com.docdoku.android.plm", "Message received: " + deliveryString);
        //TODO: handle message
    }
}
