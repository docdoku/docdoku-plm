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

import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 *
 * @author: Martin Devillers
 */
public class HttpPutTask extends AsyncTask<String, Void, Boolean> {
    private static String baseUrl;
    private static byte[] id;
    private HttpPutListener httpPutListener;

    public HttpPutTask(HttpPutListener httpPutListener) {
        super();
        if (baseUrl == null){
            this.baseUrl = HttpGetTask.baseUrl;
            this.id = HttpGetTask.id;
        }
        this.httpPutListener = httpPutListener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        Boolean result = false;
        String pURL = baseUrl + strings[0];
        Log.i("docDoku.DocDokuPLM", "Sending HttpPut request to url: " + pURL);

        try {
            URL url = new URL(pURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            conn.setRequestMethod("PUT");
            conn.connect();

            int responseCode = conn.getResponseCode();

            conn.disconnect();

            Log.i("docDoku.DocDokuPLM","Response code: " + responseCode);
            if (responseCode == 200){
                result = true;
            }

        } catch (MalformedURLException e) {
            Log.e("docDoku.DocDokuPLM","ERROR: MalformedURLException");
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("docDoku.DocDokuPLM","ERROR: ProtocolException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("docDoku.DocDokuPLM", "ERROR: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("docDoku.DocDokuPLM","ERROR: IOException");
            e.printStackTrace();
            Log.e("docDoku.DocDokuPLM", "Exception message: " + e.getMessage());
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean result){
        super.onPostExecute(result);
        if (httpPutListener != null){
            httpPutListener.onHttpPutResult(result);
        }
    }
}
