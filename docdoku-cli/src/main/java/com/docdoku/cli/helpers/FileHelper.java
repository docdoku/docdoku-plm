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


import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import org.apache.commons.codec.binary.Base64;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHelper {

    private final static int CHUNK_SIZE = 1024*8;
    private final static int BUFFER_CAPACITY = 1024*32;

    private String login;
    private String password;

    public FileHelper(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String downloadFile(File pLocalFile, String pURL) throws IOException, LoginException, NoSuchAlgorithmException {
        ConsoleProgressMonitorInputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            URL url = new URL(pURL);
            System.out.println("Downloading file: " + pLocalFile.getName() + " from " + url.getHost());
            performHeadHTTPMethod(url);

            out = new BufferedOutputStream(new FileOutputStream(pLocalFile), BUFFER_CAPACITY);

            conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestMethod("GET");
            byte[] encoded = Base64.encodeBase64((login + ":" + password).getBytes("ISO-8859-1"));
            conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
            conn.connect();
            manageHTTPCode(conn);

            MessageDigest md = MessageDigest.getInstance("MD5");
            in = new ConsoleProgressMonitorInputStream(conn.getContentLength(),new DigestInputStream(new BufferedInputStream(conn.getInputStream(), BUFFER_CAPACITY),md));
            byte[] data = new byte[CHUNK_SIZE];
            int length;

            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }
            out.flush();

            byte[] digest = md.digest();
            return Base64.encodeBase64String(digest);
        } finally {
            if(out!=null)
                out.close();
            if(in!=null)
                in.close();
            if(conn!=null)
                conn.disconnect();
        }
    }

    public String uploadFile(File pLocalFile, String pURL) throws IOException, LoginException, NoSuchAlgorithmException {
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            URL url = new URL(pURL);
            System.out.println("Uploading file: " + pLocalFile.getName() + " to " + url.getHost());
            performHeadHTTPMethod(url);


            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            byte[] encoded = Base64.encodeBase64((login + ":" + password).getBytes("ISO-8859-1"));
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
            MessageDigest md = MessageDigest.getInstance("MD5");
            in = new ConsoleProgressMonitorInputStream(pLocalFile.length(), new DigestInputStream(new BufferedInputStream(new FileInputStream(pLocalFile), BUFFER_CAPACITY),md));
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }

            out.write(footer);
            out.flush();

            manageHTTPCode(conn);

            byte[] digest = md.digest();
            return Base64.encodeBase64String(digest);
        } finally {
            if(out!=null)
                out.close();
            if(in!=null)
                in.close();
            if(conn!=null)
                conn.disconnect();
        }
    }

    private void manageHTTPCode(HttpURLConnection conn) throws IOException, LoginException {
        int code = conn.getResponseCode();
        switch (code){
            case 401: case 403:
                throw new LoginException("Error trying to login");
            case 500:
                throw new IOException(conn.getHeaderField("Reason-Phrase"));
        }
    }

    private void performHeadHTTPMethod(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(true);
        conn.setRequestProperty("Connection", "Keep-Alive");
        byte[] encoded = Base64.encodeBase64((login + ":" + password).getBytes("ISO-8859-1"));
        conn.setRequestProperty("Authorization", "Basic " + new String(encoded, "US-ASCII"));
        conn.setRequestMethod("HEAD");
        conn.connect();
        int code = conn.getResponseCode();
    }

    public static String getPartURL(URL serverURL, PartIterationKey pPart, String pRemoteFileName) throws UnsupportedEncodingException, MalformedURLException {
        return serverURL
                + "/files/"
                + URLEncoder.encode(pPart.getWorkspaceId(), "UTF-8") + "/"
                + "parts/"
                + URLEncoder.encode(pPart.getPartMasterNumber(), "UTF-8") + "/"
                + pPart.getPartRevision().getVersion() + "/"
                + pPart.getIteration() + "/nativecad/"
                + URLEncoder.encode(pRemoteFileName, "UTF-8");
    }

    public static boolean confirmOverwrite(String fileName){
        Console c = System.console();
        String response = c.readLine("The file '" + fileName + "' has been modified locally, do you want to overwrite it [y/N]?");
        return "y".equalsIgnoreCase(response);
    }

    public void uploadNativeCADFile(URL serverURL, File cadFile, PartIterationKey partIPK) throws IOException, LoginException, NoSuchAlgorithmException {
        String workspace = partIPK.getWorkspaceId();
        String fileName = cadFile.getName();
        System.out.println("Saving part: " + partIPK.getPartMasterNumber() + " " + partIPK.getPartRevision().getVersion() + "." + partIPK.getIteration() + " (" + workspace + ")");
        String digest = uploadFile(cadFile, FileHelper.getPartURL(serverURL, partIPK, fileName));

        File path = cadFile.getParentFile();
        MetaDirectoryManager meta = new MetaDirectoryManager(path);

        saveMetadata(meta, partIPK, digest, cadFile);

    }

    public void downloadNativeCADFile(URL serverURL, File path, String workspace, String partNumber, PartRevision pr, PartIteration pi, boolean force) throws IOException, LoginException, NoSuchAlgorithmException {
        BinaryResource bin = pi.getNativeCADFile();
        String fileName =  bin.getName();
        PartIterationKey partIPK = new PartIterationKey(workspace,partNumber,pr.getVersion(),pi.getIteration());
        boolean writable = (pr.isCheckedOut()) && (pr.getCheckOutUser().getLogin().equals(login)) && (pr.getLastIteration().getIteration()==pi.getIteration());
        File localFile = new File(path,fileName);
        MetaDirectoryManager meta = new MetaDirectoryManager(path);

        if(localFile.exists() && !force && localFile.lastModified()!=meta.getLastModifiedDate(localFile.getAbsolutePath())){
            boolean confirm = FileHelper.confirmOverwrite(localFile.getAbsolutePath());
            if(!confirm)
                return;
        }
        localFile.delete();
        System.out.println("Fetching part: " + partIPK.getPartMasterNumber() + " " + partIPK.getPartRevision().getVersion() + "." + partIPK.getIteration() + " (" + workspace + ")");
        String digest = downloadFile(localFile, FileHelper.getPartURL(serverURL, partIPK, fileName));
        localFile.setWritable(writable);

        saveMetadata(meta, partIPK, digest, localFile);
    }

    private void saveMetadata(MetaDirectoryManager meta, PartIterationKey partIPK, String digest, File localFile) throws IOException {
        String filePath=localFile.getAbsolutePath();

        meta.setDigest(filePath,digest);

        meta.setPartNumber(filePath,partIPK.getPartMasterNumber());
        meta.setRevision(filePath,partIPK.getPartRevision().getVersion());
        meta.setIteration(filePath,partIPK.getIteration());

        meta.setLastModifiedDate(filePath, localFile.lastModified());
    }
}
