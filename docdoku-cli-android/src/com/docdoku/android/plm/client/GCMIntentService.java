package com.docdoku.android.plm.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author: martindevillers
 */
public class GCMIntentService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }
}
