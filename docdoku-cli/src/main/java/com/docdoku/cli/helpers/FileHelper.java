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

package com.docdoku.cli.helpers;


import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.product.PartIteration;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class FileHelper {

    private final static int CHUNK_SIZE = 1024*8;
    private final static int BUFFER_CAPACITY = 1024*32;

    private String workspace;
    private String login;
    private String password;
    private String serverURL;

    public FileHelper(String serverURL, String workspace, String login, String password) {
        this.serverURL=serverURL;
        this.workspace = workspace;
        this.login = login;
        this.password = password;
    }

    public void downloadFile(File pLocalFile, String pURL) throws IOException, LoginException {
        System.out.println("Retrieving file from DocDokuPLM server: " + pLocalFile.getName());
        ConsoleProgressMonitorInputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            performHeadHTTPMethod(pURL);

            out = new BufferedOutputStream(new FileOutputStream(pLocalFile), BUFFER_CAPACITY);
            URL url = new URL(pURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestMethod("GET");
            byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((login + ":" + password).getBytes("ISO-8859-1"));
            conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
            conn.connect();
            int code = conn.getResponseCode();
            manageHTTPCode(code);

            in = new ConsoleProgressMonitorInputStream(conn.getContentLength(),new BufferedInputStream(conn.getInputStream(), BUFFER_CAPACITY));
            byte[] data = new byte[CHUNK_SIZE];
            int length;
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }
            out.flush();
        } finally {
            out.close();
            in.close();
            conn.disconnect();
        }
    }

    public void uploadFile(File pLocalFile, String pURL) throws IOException, LoginException {
        System.out.println("Saving file to DocDokuPLM server: " + pLocalFile.getName());
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            performHeadHTTPMethod(pURL);

            URL url = new URL(pURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((login + ":" + password).getBytes("ISO-8859-1"));
            conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));

            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "--------------------" + Long.toString(System.currentTimeMillis(), 16);
            byte[] header = (twoHyphens + boundary + lineEnd + "Content-Disposition: form-data; name=\"upload\";" + " filename=\"" + pLocalFile + "\"" + lineEnd + lineEnd).getBytes("ISO-8859-1");
            byte[] footer = (lineEnd + twoHyphens + boundary + twoHyphens + lineEnd).getBytes("ISO-8859-1");

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            //conn.setRequestProperty("Content-Length",len + "");
            long len = header.length + pLocalFile.length() + footer.length;
            conn.setFixedLengthStreamingMode((int) len);
            out = new BufferedOutputStream(conn.getOutputStream(), BUFFER_CAPACITY);
            out.write(header);

            byte[] data = new byte[CHUNK_SIZE];
            int length;
            in = new ConsoleProgressMonitorInputStream(pLocalFile.length(), new BufferedInputStream(new FileInputStream(pLocalFile), BUFFER_CAPACITY));
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }

            out.write(footer);
            out.flush();

            int code = conn.getResponseCode();
            manageHTTPCode(code);
            out.close();
        } catch (InterruptedIOException pEx) {
            throw pEx;
        } catch (IOException pEx) {
            out.close();
            throw pEx;
        } finally {
            in.close();
            conn.disconnect();
        }
    }

    private void manageHTTPCode(int code) throws LoginException {
        switch (code){
            case 401: case 403:
                throw new LoginException("Error trying to login.");
        }
    }
    /*
    private String getServletURL(PartIteration pPart, String pRemoteFileName) throws UnsupportedEncodingException {
        return serverURL
                + "files/"
                + URLEncoder.encode(workspace, "UTF-8") + "/"
                + "parts/"
                + URLEncoder.encode(pPart.getPartNumber(), "UTF-8") + "/"
                + pPart.getPartVersion() + "/"
                + pPart.getIteration() + "/"
                + URLEncoder.encode(pRemoteFileName, "UTF-8");
    }
    */

    private void performHeadHTTPMethod(String pURL) throws IOException {
        URL url = new URL(pURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64((login + ":" + password).getBytes("ISO-8859-1"));
        conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
        conn.setRequestMethod("HEAD");
        conn.connect();
        int code = conn.getResponseCode();
    }
}
