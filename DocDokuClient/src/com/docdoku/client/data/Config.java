/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.data;

import com.docdoku.core.entities.keys.MasterDocumentKey;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import java.io.*;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import javax.swing.filechooser.FileSystemView;

public class Config {

    private static URL sHTTPCodebase;
    
    public final static int CHUNK_SIZE = 1024*8;
    public final static int BUFFER_CAPACITY = 1024*32;
    public final static File LOCAL_TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"),"docdoku");
    
    private final static File LOCAL_CHECKOUT_FOLDER = new File(FileSystemView.getFileSystemView().getDefaultDirectory(),"docdoku");
    private final static File LOCAL_CACHE_FOLDER =  new File(LOCAL_TEMP_FOLDER, UUID.randomUUID() + File.separator + "cache");

    private Config() {
    }

    public static File getCheckOutFolder(MasterDocumentKey pMDocPK){
        return new File(LOCAL_CHECKOUT_FOLDER,MainModel.getInstance().getWorkspace() + File.separator + pMDocPK.getId() + "-" + pMDocPK.getVersion());
    }
    
    public static File getCacheFolder(MasterDocumentKey pMDocPK){
        return new File(Config.LOCAL_CACHE_FOLDER, MainModel.getInstance().getWorkspace() + File.separator + "documents" + File.separator + pMDocPK.getId() + "-" + pMDocPK.getVersion());
    }
    
    public static File getCacheFolder(MasterDocument pMDoc){
        return getCacheFolder(pMDoc.getKey());
    }
    
    public static File getCacheFolder(MasterDocumentTemplate pTemplate){
        return new File(Config.LOCAL_CACHE_FOLDER, MainModel.getInstance().getWorkspace() + File.separator + "templates" + File.separator + pTemplate.getId());
    }
    
    
    public static File getCheckOutFolder(MasterDocument pMDoc){
        return getCheckOutFolder(pMDoc.getKey());
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
