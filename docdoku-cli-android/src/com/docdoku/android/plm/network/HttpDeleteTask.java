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

package com.docdoku.android.plm.network;

import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Sends an Http DELETE request to the server.
 * <p>The constructor requires a {@link HttpDeleteListener} to notify the result of the request.
 * <p>The host's url path is specified in the first <code>String</code> parameter in the <code>execute()</code> method.
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
        HttpURLConnection conn;
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
