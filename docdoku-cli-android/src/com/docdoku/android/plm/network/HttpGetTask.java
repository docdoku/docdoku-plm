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

import android.util.Base64;
import android.util.Log;

import java.io.*;
import java.net.*;

/**
 *
 * @author: Martin Devillers
 */
public class HttpGetTask extends HttpTask<String, Void, String>{

    private HttpGetListener httpGetListener;

    public HttpGetTask(HttpGetListener httpGetListener){
        super();
        this.httpGetListener = httpGetListener;
    }

    public HttpGetTask(String host, int port, String username, String password, HttpGetListener httpGetListener) throws UnsupportedEncodingException {
        this.host = host;
        this.port = port;
        id = Base64.encode((username + ":" + password).getBytes("ISO-8859-1"), Base64.DEFAULT);
        this.httpGetListener = httpGetListener;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = ERROR_UNKNOWN;
        try {
            URL url = createURL(strings[0]);
            Log.i("com.docdoku.android.plm.client","Sending HttpGet request to url: " + url);

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
            }else{
                result = analyzeHttpErrorCode(responseCode);
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e("com.docdoku.android.plm.client","ERROR: MalformedURLException");
            result = ERROR_URL;
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
        } catch (URISyntaxException e) {
            Log.e("com.docdoku.android.plm", "URISyntaxException message: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (NullPointerException e){
            Log.e("com.docdoku.android.plm", "NullPointerException when connection to server");
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

    /**
     *
     * @author: Martin Devillers
     */
    public static interface HttpGetListener {

        void onHttpGetResult(String result);
    }
}
