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

package com.docdoku.client.data;

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentMasterTemplate;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.List;
import java.util.UUID;

public class Config {

    private static URL sHTTPCodebase;

    
    public final static String HTTP_CLIENT_STREAMING_CHUNK_SIZE= "com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size";

    public final static int CHUNK_SIZE = 1024*8;
    public final static int BUFFER_CAPACITY = 1024*32;
    public final static File LOCAL_TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"),"docdoku");

    private final static File LOCAL_CHECKOUT_FOLDER = new File(FileSystemView.getFileSystemView().getDefaultDirectory(),"docdoku");
    private final static File LOCAL_CACHE_FOLDER =  new File(LOCAL_TEMP_FOLDER, UUID.randomUUID() + File.separator + "cache");
    private final static File LOCAL_TEMP_SCAN =  new File(LOCAL_TEMP_FOLDER, UUID.randomUUID() + File.separator + "scan");

    private Config() {
    }

    public static File getTempScanFile(String fileName){
        return new File(Config.LOCAL_TEMP_SCAN, MainModel.getInstance().getWorkspace() + File.separator + fileName);
    }

    public static File getCheckOutFolder(DocumentMasterKey pDocMPK){
        return new File(LOCAL_CHECKOUT_FOLDER,MainModel.getInstance().getWorkspace() + File.separator + pDocMPK.getId() + "-" + pDocMPK.getVersion());
    }

    public static File getCacheFolder(DocumentMasterKey pDocMPK){
        return new File(Config.LOCAL_CACHE_FOLDER, MainModel.getInstance().getWorkspace() + File.separator + "documents" + File.separator + pDocMPK.getId() + "-" + pDocMPK.getVersion());
    }

    public static File getCacheFolder(DocumentMaster pDocM){
        return getCacheFolder(pDocM.getKey());
    }

    public static File getCacheFolder(DocumentMasterTemplate pTemplate){
        return new File(Config.LOCAL_CACHE_FOLDER, MainModel.getInstance().getWorkspace() + File.separator + "document-templates" + File.separator + pTemplate.getId());
    }

    public static String getPermaLink(DocumentMaster pDocM){
        String file = null;
        try {
            file = "documents/"
                    + URLEncoder.encode(MainModel.getInstance().getWorkspace().getId(),"UTF-8") + "/"
                    + URLEncoder.encode(pDocM.getId(),"UTF-8") + "/"
                    + pDocM.getVersion();
        } catch (UnsupportedEncodingException pEx) {
            System.err.println(pEx.getMessage());
        }
        return getHTTPCodebase().toString() + file;
    }

    public static File getCheckOutFolder(DocumentMaster pDocM){
        return getCheckOutFolder(pDocM.getKey());
    }

    public static Proxy getProxy(URI pURI){
        List<Proxy> proxies = ProxySelector.getDefault().select(pURI);
        return proxies.get(0);
    }

    public static Proxy getProxy(String pURI) throws URISyntaxException{
        return getProxy(new URI(pURI));
    }

    public static URL getHTTPCodebase(){
        return sHTTPCodebase;
    }

    public static void setHTTPCodebase(URL pHTTPCodebase){
        sHTTPCodebase=pHTTPCodebase;
    }
}