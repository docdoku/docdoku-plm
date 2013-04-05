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

import org.junit.Test;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Asmae Chadid
 */
public class GoogleStorageCloud {

    private final static int CHUNK_SIZE = 1024;
    private String vaultPath="/var/lib/docdoku/vault/";
    private String OAuth = "Bearer ";
    private String method;
    private HttpURLConnection httpConn;
    private String fileName;
    private GoogleStorageProperties properties = new GoogleStorageProperties();

    public File getFile(String fileName) throws IOException {

        this.fileName = fileName;
        this.method = "GET";
        this.init();
        if (httpConn.getResponseCode() != 200) {
            throw new IOException(httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
        } else {

            DataInputStream dataInputStream = new DataInputStream(httpConn.getInputStream());
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

        if (fileName.contains(vaultPath)) {
            this.fileName = fileName.substring(vaultPath.length());
        } else {
            this.fileName = fileName;
        }

        this.init();
        DataOutputStream wr = new DataOutputStream(httpConn.getOutputStream());
        InputStream in = inputStream;

        byte[] buf = new byte[CHUNK_SIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
            wr.write(buf, 0, len);
        }
        in.close();
        wr.flush();
        wr.close();


        if (httpConn.getResponseCode() != 200) {
            throw new IOException(httpConn.getResponseCode() + " " +
                    httpConn.getResponseMessage());
        }
        return wr.size();
    }

    public void delete(String fileName) throws IOException {
        this.fileName = fileName;
        this.method = "DELETE";
        this.init();
        if (httpConn.getResponseCode() != 204) {
            throw new IOException(httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
        }
    }

    private void init() throws IOException{
        OAuth += getOAuthAccessToken();
        URL url = new URL(properties.getURI() + fileName);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod(method);
        httpConn.setRequestProperty("Host", properties.getHost());
        httpConn.setRequestProperty("Content-Length", "1");
        httpConn.setRequestProperty("Authorization", OAuth);
        httpConn.setRequestProperty("x-goog-api-version", properties.getApiVersion());
        httpConn.setRequestProperty("x-goog-project-id", properties.getProjectId());
        httpConn.setDoOutput(true);
    }

    private String getOAuthAccessToken()  throws IOException{
        Oauth2TokenGetter oAuthPath = new Oauth2TokenGetter();
        return oAuthPath.getToken();
    }

}
