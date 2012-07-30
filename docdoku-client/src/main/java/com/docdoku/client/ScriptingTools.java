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

import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.services.IUploadDownloadWS;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

/**
 *
 * @author Florent GARIN
 */
public class ScriptingTools {
    
    public final static String DEFAULT_DOCUMENT_WSDL_LOCATION="http://localhost:8080/services/document?wsdl";
    public final static String DEFAULT_PRODUCT_WSDL_LOCATION="http://localhost:8080/services/product?wsdl";   
    public final static String DEFAULT_FILE_MANAGER_WSDL_LOCATION="http://localhost:8080/services/UploadDownload?wsdl";
    
    
    private ScriptingTools(){}
    
    public static IDocumentManagerWS createDocumentService(String url, String login, String password) throws MalformedURLException, Exception{
        DocumentService service = new DocumentService(new URL(url),new javax.xml.namespace.QName("http://server.docdoku.com/", "DocumentManagerBeanService"));
        IDocumentManagerWS port= service.getPort(IDocumentManagerWS.class);
        ((BindingProvider)port).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider)port).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password);
     
        return port;
    }

    public static IProductManagerWS createProductService(String url, String login, String password) throws MalformedURLException, Exception{
        ProductService service = new ProductService(new URL(url),new javax.xml.namespace.QName("http://server.docdoku.com/", "ProductManagerBeanService"));
        IProductManagerWS port= service.getPort(IProductManagerWS.class);
        ((BindingProvider)port).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider)port).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password);
     
        return port;
    }    
    
    public static IUploadDownloadWS createFileManagerService(String url, String login, String password) throws MalformedURLException{
        MTOMFeature feature = new MTOMFeature();
        UploadDownloadService service = new UploadDownloadService(new URL(url),new javax.xml.namespace.QName("http://server.docdoku.com/", "UploadDownloadService"));
        IUploadDownloadWS proxy = service.getPort(IUploadDownloadWS.class,feature);
        Map context = ((BindingProvider)proxy).getRequestContext();
        context.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        context.put( BindingProvider.USERNAME_PROPERTY, login);
        context.put( BindingProvider.PASSWORD_PROPERTY, password);
        return proxy;
    }
    
    public static IProductManagerWS createProductService(String login, String password) throws MalformedURLException, Exception{
        return createProductService(DEFAULT_PRODUCT_WSDL_LOCATION,login,password);
    }
    
    public static IDocumentManagerWS createDocumentService(String login, String password) throws MalformedURLException, Exception{
        return createDocumentService(DEFAULT_DOCUMENT_WSDL_LOCATION,login,password);
    }

    public static IUploadDownloadWS createFileManagerService(String login, String password) throws MalformedURLException, Exception{
        return createFileManagerService(DEFAULT_FILE_MANAGER_WSDL_LOCATION,login,password);
    }
    
}
