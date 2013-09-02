package com.docdoku.android.plm.network;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.net.*;

/**
 * @author: martindevillers
 */
public class HttpDeleteTask extends HttpTask<String, Void, Boolean> {
    private static final String LOG_TAG = "com.docdoku.android.plm.network.HttpDeleteTask";

    private HttpDeleteListener listener;

    public HttpDeleteTask(HttpDeleteListener listener){
        super();
        this.listener = listener;
    }


    @Override
    protected Boolean doInBackground(String... strings) {
        boolean result = false;
        HttpURLConnection conn = null;
        try {
            URL url = createURL(strings[0]);
            Log.i(LOG_TAG, "Sending Http request to URL: " + url);

            conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.setRequestProperty("Content-Type","application/json");

            conn.connect();
            int responseCode = conn.getResponseCode();
            conn.disconnect();

            Log.i(LOG_TAG, "Response code: " + responseCode);
            if (responseCode == 200){
                result = true;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    @Override
    public void onPostExecute(Boolean b){
        if (listener != null){
            listener.onHttpDeleteResult(b);
        }
    }

    public static interface HttpDeleteListener{
        public void onHttpDeleteResult(boolean result);
    }
}
