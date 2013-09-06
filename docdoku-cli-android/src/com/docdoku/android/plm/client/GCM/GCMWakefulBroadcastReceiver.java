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

package com.docdoku.android.plm.client.GCM;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * <code>BroadcastReceiver</code> that is called when a Google Cloud Message is received on the device, targeting this application's package.
 *
 * @author: martindevillers
 */
public class GCMWakefulBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String LOG_TAG = "com.docdoku.android.plm.client.GCM.GCMWakefulBroadcastReceiver";

    /**
     * Called when a message is received. The message is then passed to the {@link GCMIntentService} to be processed.
     *
     * @param context The current <code>Context</code>
     * @param intent The intent containing information about the message and the message itself
     * @see WakefulBroadcastReceiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "Broadcast receiver started by intent");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
