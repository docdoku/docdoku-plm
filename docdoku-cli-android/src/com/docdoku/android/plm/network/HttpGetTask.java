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
import com.docdoku.android.plm.client.Session;

import java.io.*;
import java.net.*;

/**
 * Sends an Http GET request to the server, receiving a simple <code>String</code> result.
 * <p>The constructor requires a {@link HttpGetListener} to notify the result of the request.
 * <p>The host's url path is specified in the first <code>String</code> parameter in the <code>execute()</code> method.
 * <p>
 * <p>This method has a second constructors with a <code>Session</code> parameter, that defines the server connection
 * information for all Http methods.
 * @author: Martin Devillers
 */
public class HttpGetTask extends HttpTask<String, Void, String>{
    private static final String LOG_TAG = "com.docdoku.android.plm.network.HttpGetTask";

    private HttpGetListener httpGetListener;

    public HttpGetTask(HttpGetListener httpGetListener){
        super();
        this.httpGetListener = httpGetListener;
    }

    public HttpGetTask(Session session, HttpGetListener httpGetListener) throws UnsupportedEncodingException {
        this.host = session.getHost();
        this.port = session.getPort();
        id = Base64.encode((session.getUserLogin() + ":" + session.getPassword()).getBytes("ISO-8859-1"), Base64.DEFAULT);
        this.httpGetListener = httpGetListener;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = ERROR_UNKNOWN;
        try {
            URL url = createURL(strings[0]);
            Log.i(LOG_TAG,"Sending HttpGet request to url: " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.connect();

            int responseCode = conn.getResponseCode();
            Log.i(LOG_TAG,"Response code: " + responseCode);
            if (responseCode == 200){
                Log.i(LOG_TAG, "Response headers: " + conn.getHeaderFields());
                Log.i(LOG_TAG, "Response message: " + conn.getResponseMessage());
                InputStream in = (InputStream) conn.getContent();
                result = inputStreamToString(in);
                Log.i(LOG_TAG, "Response content: " + result);
                in.close();
            }else{
                result = analyzeHttpErrorCode(responseCode);
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG,"ERROR: MalformedURLException");
            result = ERROR_URL;
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e(LOG_TAG,"ERROR: ProtocolException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "ERROR: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG,"ERROR: IOException");
            e.printStackTrace();
            Log.e(LOG_TAG, "Exception message: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e){
            Log.e(LOG_TAG, "ERROR: No Url provided for the Get query");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "URISyntaxException message: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (NullPointerException e){
            Log.e(LOG_TAG, "NullPointerException when connection to server");
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
