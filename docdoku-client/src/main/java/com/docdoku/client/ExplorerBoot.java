/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.client;

import com.docdoku.core.services.IUploadDownloadWS;
import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.client.actions.LoginActionListener;
import com.docdoku.client.data.Prefs;
import com.docdoku.client.ui.login.LoginFrame;
import com.docdoku.client.actions.MainController;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.data.Config;
import com.docdoku.core.util.FileIO;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import javax.swing.*;
import java.net.URL;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;



public class ExplorerBoot {
    
    
    
    public static void main(String[] args) {       
        init();
        try {
            String webContext=args[0].split("/")[3];
            URL webURL;
            if(webContext.equals("apps"))
                webURL=new URL(new URL(args[0]),"/");
            else
                webURL=new URL(new URL(args[0]),"/"+ webContext + "/");
            URL documentServiceURL = new URL(webURL,"/services/document?wsdl");
            URL uploadDownloadServiceURL = new URL(webURL,"/services/UploadDownload?wsdl");
            Config.setHTTPCodebase(webURL);
            MainController.init(lookupDocumentWebService(documentServiceURL), lookupUploadDownloadWebService(uploadDownloadServiceURL));
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    LoginFrame frame = new LoginFrame(new LoginActionListener());
                }
            });
            
        } catch (Exception pEx) {
            String message = pEx.getMessage()==null?I18N.BUNDLE
                    .getString("Error_unknown"):pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE
                    .getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
            pEx.printStackTrace();
            System.exit(-1);
        }
    }
    public static IUploadDownloadWS lookupUploadDownloadWebService(URL pURL){
        
        MTOMFeature feature = new MTOMFeature();
        UploadDownloadService service = new UploadDownloadService(pURL,new javax.xml.namespace.QName("http://server.docdoku.com/", "UploadDownloadService"));
        IUploadDownloadWS proxy = service.getPort(IUploadDownloadWS.class,feature);
        ((BindingProvider)proxy).getRequestContext().put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        //Map context = ((BindingProvider)proxy).getRequestContext();
        return proxy;
    }

    public static IDocumentManagerWS lookupDocumentWebService(URL pURL) throws MalformedURLException{
        DocumentService service = new DocumentService(pURL,new javax.xml.namespace.QName("http://server.docdoku.com/", "DocumentManagerBeanService"));
        return service.getPort(IDocumentManagerWS.class);
        
    }
    
    
    private static void init() {
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        Prefs.initLocale();
        FileIO.rmDir(Config.LOCAL_TEMP_FOLDER);
        try {           
            UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueMoonLookAndFeel");
        } catch (Exception pEx) {
            System.err.println("Look And Feel Exception.");
            System.err.println(pEx.getMessage());
        }
    }
}
