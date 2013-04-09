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

package com.docdoku.server.vault.googlestorage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Asmae Chadid
 */
public class GoogleStorageCloud {

    private final static int CHUNK_SIZE = 1024;
    private String OAuth = "Bearer ";
    private String method;
    private HttpURLConnection httpConnection;
    private String fileName;
    private GoogleStorageProperties properties = new GoogleStorageProperties();

    public File getFile(String fileName) throws IOException {

        this.fileName = fileName;
        this.method = "GET";
        this.init();
        if (httpConnection.getResponseCode() != 200) {
            throw new IOException(httpConnection.getResponseCode() + " " + httpConnection.getResponseMessage());
        } else {

            DataInputStream dataInputStream = new DataInputStream(httpConnection.getInputStream());
            File file = new File(fileName.split("/")[fileName.split("/").length - 1]);
            FileOutputStream outputStream = new FileOutputStream(file);
            DataOutputStream wr = new DataOutputStream(outputStream);
            byte[] buf = new byte[CHUNK_SIZE];
            int len;
            while ((len = dataInputStream.read(buf)) > 0) {
                wr.write(buf, 0, len);
            }
            dataInputStream.close();
            wr.flush();
            wr.close();

            return file;
        }
    }

    public int upload(InputStream inputStream, String fileName) throws IOException {
        this.method = "PUT";

        this.init();
        DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
        InputStream in = inputStream;

        byte[] buf = new byte[CHUNK_SIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
            wr.write(buf, 0, len);
        }
        in.close();
        wr.flush();
        wr.close();


        if (httpConnection.getResponseCode() != 200) {
            throw new IOException(httpConnection.getResponseCode() + " " +
                    httpConnection.getResponseMessage());
        }
        return wr.size();
    }

    public void delete(String fileName) throws IOException {
        this.fileName = fileName;
        this.method = "DELETE";
        this.init();
        if (httpConnection.getResponseCode() != 204) {
            throw new IOException(httpConnection.getResponseCode() + " " + httpConnection.getResponseMessage());
        }
    }

    private void init() throws IOException{
        OAuth += getOAuthAccessToken();
        URL url = new URL(properties.getURI() + fileName);
        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod(method);
        httpConnection.setRequestProperty("Host", properties.getHost());
        httpConnection.setRequestProperty("Content-Length", "1");
        httpConnection.setRequestProperty("Authorization", OAuth);
        httpConnection.setRequestProperty("x-goog-api-version", properties.getApiVersion());
        httpConnection.setRequestProperty("x-goog-project-id", properties.getProjectId());
        httpConnection.setDoOutput(true);
    }

    private String getOAuthAccessToken()  throws IOException{
        Oauth2TokenGetter oAuthPath = new Oauth2TokenGetter();
        return oAuthPath.getToken();
    }

}
