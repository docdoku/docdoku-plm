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
import android.os.Environment;
import android.util.Log;
import com.docdoku.android.plm.network.listeners.HttpGetDownloadFileListener;

import java.io.*;
import java.net.*;

/**
 *
 * @author: Martin Devillers
 */
public class HttpGetDownloadFileTask extends HttpTask <String, Integer, Boolean> {

    HttpGetDownloadFileListener httpGetDownloadFileListener;
    private String fileSavePath;

    public HttpGetDownloadFileTask(HttpGetDownloadFileListener httpGetDownloadFileListener){
        super();
        this.httpGetDownloadFileListener = httpGetDownloadFileListener;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        boolean result = false;
        HttpURLConnection conn = null;
        String filename = strings[1];
        try {
            URL url = createURL(strings[0]);
            Log.i("com.docdoku.android.plm.client", "Sending HttpGet request to download_light file at url: " + url);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            fileSavePath = file.getAbsolutePath();
            Log.i("com.docdoku.android.plm.client", "Path to which file is being saved: " + fileSavePath);
            FileOutputStream outputStream = new FileOutputStream(file);

            conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Basic " + new String(id, "US-ASCII"));
            conn.connect();
            Log.i("com.docdoku.android.plm.client", "Response code: " + conn.getResponseCode());

            InputStream inputStream = conn.getInputStream();
            int totalSize = conn.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            int responseCode = conn.getResponseCode();
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                outputStream.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;

                publishProgress(downloadedSize, totalSize);
            }
            outputStream.close();
            inputStream.close();
            result = (responseCode == 200);
        } catch (UnsupportedEncodingException e) {
            Log.e("com.docdoku.android.plm.client","UnsupportedEncodingException in file download");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ProtocolException e) {
            Log.e("com.docdoku.android.plm.client","ProtocolException in file download");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            Log.e("com.docdoku.android.plm.client","MalformedURLException in file download");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            Log.e("com.docdoku.android.plm.client","IOException in file download");
            Log.e("com.docdoku.android.plm.client", "Error message: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if(conn!=null)
            conn.disconnect();
        return result;
    }

    @Override
    public void onProgressUpdate(Integer... values){
        float progress = values[0];
        float size = values[1];
        float advancement = progress/size * 100;
        if (httpGetDownloadFileListener != null){
            httpGetDownloadFileListener.onProgressUpdate((int) advancement);
        }
    }

    @Override
    public void onPostExecute(Boolean result){
        if (httpGetDownloadFileListener != null){
            httpGetDownloadFileListener.onFileDownloaded(result, fileSavePath);
        }
    }

    @Override
    public void onPreExecute(){
        if (httpGetDownloadFileListener != null){
            httpGetDownloadFileListener.onFileDownloadStart();
        }
    }
}
