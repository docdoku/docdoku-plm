/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentRevision;
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
import java.util.Locale;
import java.util.Set;

public class FileHelper {

    private static final int CHUNK_SIZE = 1024*8;
    private static final int BUFFER_CAPACITY = 1024*32;

    private String login;
    private String password;
    private CliOutput output;
    private Locale locale;

    public FileHelper(String login, String password, CliOutput output, Locale locale) {
        this.login = login;
        this.password = password;
        this.output = output;
        this.locale = locale;
    }

    private String downloadFile(File pLocalFile, String pURL) throws IOException, LoginException, NoSuchAlgorithmException {
        FilterInputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negotiate the NTLM proxy authentication
            URL url = new URL(pURL);

            output.printInfo(
                    LangHelper.getLocalizedMessage("DownloadingFile",locale)
                    + " : "
                    + pLocalFile.getName() + " "
                    + LangHelper.getLocalizedMessage("From",locale) + " "
                    + url.getHost());

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
            in = output.getMonitor(conn.getContentLength(),new DigestInputStream(new BufferedInputStream(conn.getInputStream(), BUFFER_CAPACITY),md));

            byte[] data = new byte[CHUNK_SIZE];
            int length;

            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }
            out.flush();

            byte[] digest = md.digest();
            return Base64.encodeBase64String(digest);
        } finally {
            if(out!=null) {
                out.close();
            }
            if(in!=null) {
                in.close();
            }
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }

    private String uploadFile(File pLocalFile, String pURL) throws IOException, LoginException, NoSuchAlgorithmException {
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        try {
            //Hack for NTLM proxy
            //perform a head method to negociate the NTLM proxy authentication
            URL url = new URL(pURL);

            output.printInfo(
                    LangHelper.getLocalizedMessage("UploadingFile",locale)
                    + " : "
                    + pLocalFile.getName() + " "
                    + LangHelper.getLocalizedMessage("To",locale) + " "
                    + url.getHost());
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
            byte[] header = (twoHyphens + boundary + lineEnd + "Content-Disposition: form-data; name=\"upload\";" + " filename=\"" + pLocalFile.getName() + "\"" + lineEnd + lineEnd).getBytes("ISO-8859-1");
            byte[] footer = (lineEnd + twoHyphens + boundary + twoHyphens + lineEnd).getBytes("ISO-8859-1");

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            long len = header.length + pLocalFile.length() + footer.length;
            conn.setFixedLengthStreamingMode((int) len);
            out = new BufferedOutputStream(conn.getOutputStream(), BUFFER_CAPACITY);
            out.write(header);

            byte[] data = new byte[CHUNK_SIZE];
            int length;
            MessageDigest md = MessageDigest.getInstance("MD5");
            in = output.getMonitor(pLocalFile.length(), new DigestInputStream(new BufferedInputStream(new FileInputStream(pLocalFile), BUFFER_CAPACITY),md));
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }

            out.write(footer);
            out.flush();

            manageHTTPCode(conn);

            byte[] digest = md.digest();
            return Base64.encodeBase64String(digest);
        } finally {
            if(out!=null) {
                out.close();
            }
            if(in!=null) {
                in.close();
            }
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }

    private void manageHTTPCode(HttpURLConnection conn) throws IOException, LoginException {
        int code = conn.getResponseCode();
        switch (code){
            case 401: case 403:
                throw new LoginException(LangHelper.getLocalizedMessage("LoginError",locale));
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
    }

    private static String getPartURL(URL serverURL, PartIterationKey pPartIPK, String pRemoteFileName) throws UnsupportedEncodingException, MalformedURLException {
        return serverURL
                + "/api/files/"
                + URLEncoder.encode(pPartIPK.getWorkspaceId(), "UTF-8") + "/"
                + "parts/"
                + URLEncoder.encode(pPartIPK.getPartMasterNumber(), "UTF-8") + "/"
                + pPartIPK.getPartRevision().getVersion() + "/"
                + pPartIPK.getIteration() + "/nativecad/"
                + pRemoteFileName;
    }

    private static String getDocumentURL(URL serverURL, DocumentIterationKey pDocIPK, String pRemoteFileName) throws UnsupportedEncodingException, MalformedURLException {
        return serverURL
                + "/api/files/"
                + URLEncoder.encode(pDocIPK.getWorkspaceId(), "UTF-8") + "/"
                + "documents/"
                + URLEncoder.encode(pDocIPK.getDocumentMasterId(), "UTF-8") + "/"
                + pDocIPK.getDocumentRevision().getVersion() + "/"
                + pDocIPK.getIteration() + "/"
                + pRemoteFileName;
    }

    public static String getPartURLUpload(URL serverURL, PartIterationKey pPart) throws UnsupportedEncodingException, MalformedURLException {
        return serverURL
                + "/api/files/"
                + URLEncoder.encode(pPart.getWorkspaceId(), "UTF-8") + "/"
                + "parts/"
                + URLEncoder.encode(pPart.getPartMasterNumber(), "UTF-8") + "/"
                + pPart.getPartRevision().getVersion() + "/"
                + pPart.getIteration() + "/nativecad/";
    }

    private static String getDocumentURLUpload(URL serverURL, DocumentIterationKey docIPK) throws UnsupportedEncodingException {
        return serverURL
                + "/api/files/"
                + URLEncoder.encode(docIPK.getWorkspaceId(), "UTF-8") + "/"
                + "documents/"
                + URLEncoder.encode(docIPK.getDocumentMasterId(), "UTF-8") + "/"
                + docIPK.getDocumentRevision().getVersion() + "/"
                + docIPK.getIteration();
    }

    public static boolean confirmOverwrite(String fileName){
        Console c = System.console();
        String response = c.readLine("The file '" + fileName + "' has been modified locally, do you want to overwrite it [y/N]?");
        return "y".equalsIgnoreCase(response);
    }

    public void uploadNativeCADFile(URL serverURL, File cadFile, PartIterationKey partIPK) throws IOException, LoginException, NoSuchAlgorithmException {
        String digest = uploadFile(cadFile, FileHelper.getPartURLUpload(serverURL, partIPK));

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
            if(!confirm) {
                return;
            }
        }
        localFile.delete();
        String digest = downloadFile(localFile, FileHelper.getPartURL(serverURL, partIPK, fileName));
        localFile.setWritable(writable);

        saveMetadata(meta, partIPK, digest, localFile);
    }

    private void saveMetadata(MetaDirectoryManager meta, PartIterationKey partIPK, String digest, File localFile) throws IOException {
        String filePath=localFile.getAbsolutePath();
        meta.setDigest(filePath,digest);
        meta.setPartNumber(filePath,partIPK.getPartMasterNumber());
        meta.setWorkspace(filePath,partIPK.getWorkspaceId());
        meta.setRevision(filePath,partIPK.getPartRevision().getVersion());
        meta.setIteration(filePath,partIPK.getIteration());
        meta.setLastModifiedDate(filePath, localFile.lastModified());
    }

    private void saveMetadata(MetaDirectoryManager meta, DocumentIterationKey docIPK, String digest, File localFile) throws IOException {
        String filePath=localFile.getAbsolutePath();
        meta.setDigest(filePath,digest);
        meta.setDocumentId(filePath,docIPK.getDocumentMasterId());
        meta.setWorkspace(filePath,docIPK.getWorkspaceId());
        meta.setRevision(filePath,docIPK.getDocumentRevision().getVersion());
        meta.setIteration(filePath,docIPK.getIteration());
        meta.setLastModifiedDate(filePath, localFile.lastModified());
    }

    public void downloadDocumentFiles(URL serverURL, File path, String workspace, String id, DocumentRevision dr, DocumentIteration di, boolean force) throws IOException, LoginException, NoSuchAlgorithmException {
        Set<BinaryResource> bins = di.getAttachedFiles();
        for(BinaryResource bin:bins){
            String fileName =  bin.getName();


            DocumentIterationKey docIPK = new DocumentIterationKey(workspace,id,dr.getVersion(),di.getIteration());
            boolean writable = (dr.isCheckedOut()) && (dr.getCheckOutUser().getLogin().equals(login)) && (dr.getLastIteration().getIteration()==di.getIteration());
            File localFile = new File(path,fileName);
            MetaDirectoryManager meta = new MetaDirectoryManager(path);

            if(localFile.exists() && !force && localFile.lastModified()!=meta.getLastModifiedDate(localFile.getAbsolutePath())){
                boolean confirm = FileHelper.confirmOverwrite(localFile.getAbsolutePath());
                if(!confirm) {
                    return;
                }
            }

            localFile.delete();
            String digest = downloadFile(localFile, FileHelper.getDocumentURL(serverURL, docIPK, fileName));
            localFile.setWritable(writable);

            saveMetadata(meta, docIPK, digest, localFile);

        }
    }


    public void uploadDocumentFile(URL serverURL, File file, DocumentIterationKey docIPK) throws IOException, LoginException, NoSuchAlgorithmException {
        String digest = uploadFile(file, FileHelper.getDocumentURLUpload(serverURL, docIPK));

        File path = file.getParentFile();
        MetaDirectoryManager meta = new MetaDirectoryManager(path);

        saveMetadata(meta, docIPK, digest, file);
    }

}
