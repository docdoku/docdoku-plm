package com.docdoku.android.plm.client;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.docdoku.android.plm.network.HttpPutTask;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author: martindevillers
 */
public class GCMRegisterService extends Service {

    public static final String INTENT_KEY_ACTION = "register/unregister";
    public static final int ACTION_SEND_ID = 1;
    public static final int ACTION_ERASE_ID = 2;

    private static final String PREFERENCES_GCM = "GCM";
    private static final String PREFERENCE_KEY_GCM_ID = "gcm id";
    private static final String PREFERENCE_KEY_GCM_REGISTRATION_VERSION = "gcm version";
    private static final String PREFERENCE_KEY_GCM_EXPIRATION_DATE = "gcm expiration";

    private static final String SENDER_ID = "263093437022"; //See Google API Console to set Id
    private static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7; //Default lifespan (7 days) of a reservation until it is considered expired.

    private static final String JSON_KEY_GCM_ID = "gcmId";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int j){
        Bundle bundle = intent.getExtras();
        if (bundle!=null){
            int action = intent.getExtras().getInt(INTENT_KEY_ACTION);
            switch (action){
                case ACTION_SEND_ID:
                    getGCMId();
                    break;
                case ACTION_ERASE_ID:
                    sendGCMId("");
                    break;
                default:
                    Log.w("com.docdoku.android.plm", "No code provided for GCM Registration service");
                    break;
            }
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }

    private void getGCMId() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_GCM, MODE_PRIVATE);
        String gcmId = preferences.getString(PREFERENCE_KEY_GCM_ID, null);
        Log.i("com.docdoku.android.plm", "Looking for gcm Id...");
        if (gcmId == null){
            Log.i("com.docdoku.android.plm", "No gcm Id was found in storage");
            getNewGCMId();
        }else{
            try {
                int gcmAppVersion = preferences.getInt(PREFERENCE_KEY_GCM_REGISTRATION_VERSION, -1);
                long expirationTime = preferences.getLong(PREFERENCE_KEY_GCM_EXPIRATION_DATE, -1);
                if (isGCMIdExpired(expirationTime) || isGCMIdPreviousVersion(gcmAppVersion)){
                    Log.i("com.docdoku.android.plm", "gcm Id belonged to previoud app version or was expired");
                    getNewGCMId();
                }else{
                    Log.i("com.docdoku.android.plm", "gcm Id found! " + gcmId);
                    sendGCMId(gcmId);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("com.docdoku.android.plm", "Could not get package name");
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private boolean isGCMIdPreviousVersion(int gcmAppVersion) throws PackageManager.NameNotFoundException {
        int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        return currentVersion != gcmAppVersion;
    }

    private boolean isGCMIdExpired(long expirationTime){
        return System.currentTimeMillis() > expirationTime;
    }

    private void getNewGCMId(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... objects) {
                GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(GCMRegisterService.this);
                try {
                    String gcmId = googleCloudMessaging.register(SENDER_ID);
                    Log.i("com.docdoku.android.plm", "gcm Id obtained: " + gcmId);
                    saveGCMId(gcmId);
                    sendGCMId(gcmId);
                } catch (IOException e) {
                    Log.e("com.docdoku.android.plm", "IOException when registering for gcm Id");
                    Log.e("com.docdoku.android.plm", "Exception message: " + e.getMessage());
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("com.docdoku.android.plm", "Exception when trying to retrieve app version corresponding to new gcm Id");
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }.execute();
    }

    private void saveGCMId(String gcmId) throws PackageManager.NameNotFoundException {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_GCM, MODE_PRIVATE);
        preferences.edit()
            .putString(PREFERENCE_KEY_GCM_ID, gcmId)
            .putInt(PREFERENCE_KEY_GCM_REGISTRATION_VERSION, getPackageManager().getPackageInfo(getPackageName(), 0).versionCode)
            .putLong(PREFERENCE_KEY_GCM_EXPIRATION_DATE, System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS)
            .commit();
    }

    private void sendGCMId(String gcmId){
        Log.i("com.docdoku.android.plm", "Sending gcm id to server");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_KEY_GCM_ID, gcmId);
            new HttpPutTask(null).execute("/api/accounts/gcm", jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        stopSelf();
    }
}
