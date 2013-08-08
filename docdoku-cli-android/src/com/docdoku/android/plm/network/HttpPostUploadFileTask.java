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

import android.os.AsyncTask;
import android.util.Log;
import com.docdoku.android.plm.network.listeners.HttpPostUploadFileListener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: martindevillers
 */
public class HttpPostUploadFileTask extends AsyncTask<String, Void, Boolean>{

    private final static int CHUNK_SIZE = 1024*8;
    private final static int BUFFER_CAPACITY = 1024*32;

    private byte[] id;
    private String baseUrl;
    private String fileName;

    private HttpPostUploadFileListener listener;
    
    public HttpPostUploadFileTask(HttpPostUploadFileListener listener){
        id = HttpGetTask.id;
        baseUrl = HttpGetTask.baseUrl;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        boolean result = false;
        HttpURLConnection conn = null;
        try{
            fileName = strings[0];
            String pUrl = baseUrl + fileName;
            String filePath = strings[1];

            URL url = new URL(pUrl);
            Log.i("com.docdoku.android.plm.client", "Sending HttpPost request to upload file at from path: " + filePath + " to Url: "+ pUrl);
            File file = new File(filePath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestMethod("POST");

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "--------------------" + Long.toString(System.currentTimeMillis(), 16);
            byte[] header = (twoHyphens + boundary + lineEnd + "Content-Disposition: form-data; name=\"upload\";" + " filename=\"" + file + "\"" + lineEnd + lineEnd).getBytes("ISO-8859-1");
            byte[] footer = (lineEnd + twoHyphens + boundary + twoHyphens + lineEnd).getBytes("ISO-8859-1");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            long len = header.length + file.length() + footer.length;
            conn.setFixedLengthStreamingMode((int) len);
            conn.connect();
            OutputStream out = new BufferedOutputStream(conn.getOutputStream(), BUFFER_CAPACITY);
            out.write(header);

            byte[] data = new byte[CHUNK_SIZE];
            int length;

            FileInputStream fileInputStream = new FileInputStream(file);
            while ((length = fileInputStream.read(data)) != -1) {
                out.write(data, 0, length);
            }

            out.write(footer);
            out.flush();

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            result = (responseCode == 200);
            Log.i("com.docdoku.android.plm.client", "HttpPostUploadFileTask Response code: " + responseCode);
        } catch (ArrayIndexOutOfBoundsException e){
            Log.e("com.docdoku.android.plm.client","ArrayIndexOutOfBoundsException: not enough arguments passed to upload file");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.e("com.docdoku.android.plm.client","MalformedURLException: failed to upload file");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            Log.e("com.docdoku.android.plm.client","UnsupportedEncodingException: failed to upload file");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            Log.e("com.docdoku.android.plm.client","IOException: failed to upload file");
            Log.e("com.docdoku.android.plm.client","IOException message: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return result;
    }

    @Override
    public void onPostExecute(Boolean result){
        listener.onUploadResult(result, fileName);
    }

}
