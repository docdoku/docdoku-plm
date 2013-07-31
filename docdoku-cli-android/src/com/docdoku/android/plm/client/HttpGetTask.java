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
import android.util.Base64;
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
public class HttpGetTask extends AsyncTask<String, Void, String>{

    public static String baseUrl;
    public static byte[] id;
    private HttpGetListener httpGetListener;

    public static final String CONNECTION_ERROR = "Connection Error";
    public static final String URL_ERROR = "Url error";

    public HttpGetTask(HttpGetListener httpGetListener){
        super();
        this.httpGetListener = httpGetListener;
    }

    public HttpGetTask(String url, String username, String password, HttpGetListener httpGetListener) throws UnsupportedEncodingException {
        super();
        baseUrl = url;
        id = Base64.encode((username + ":" + password).getBytes("ISO-8859-1"), Base64.DEFAULT);
        this.httpGetListener = httpGetListener;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = CONNECTION_ERROR;

        try {
            String pURL = baseUrl + strings[0];
            Log.i("com.docdoku.android.plm.client","Sending HttpGet request to url: " + pURL);
            URL url = new URL(pURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.connect();

            int responseCode = conn.getResponseCode();
            Log.i("com.docdoku.android.plm.client","Response code: " + responseCode);
            if (responseCode == 200){
                Log.i("com.docdoku.android.plm.client", "Response headers: " + conn.getHeaderFields());
                Log.i("com.docdoku.android.plm.client", "Response message: " + conn.getResponseMessage());
                InputStream in = (InputStream) conn.getContent();
                result = inputStreamToString(in);
                Log.i("com.docdoku.android.plm.client", "Response content: " + result);
                in.close();
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e("com.docdoku.android.plm.client","ERROR: MalformedURLException");
            result = URL_ERROR;
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("com.docdoku.android.plm.client","ERROR: ProtocolException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("com.docdoku.android.plm.client", "ERROR: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("com.docdoku.android.plm.client","ERROR: IOException");
            e.printStackTrace();
            Log.e("com.docdoku.android.plm.client", "Exception message: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e){
            Log.e("com.docdoku.android.plm.client", "ERROR: No Url provided for the Get query");
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);
        if (httpGetListener != null){
            httpGetListener.onHttpGetResult(result);
        }
    }

    private String inputStreamToString(InputStream in) throws IOException {
        String string;
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bf = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bf.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        string = sb.toString();
        reader.close();
        bf.close();
        return string;
    }

}
