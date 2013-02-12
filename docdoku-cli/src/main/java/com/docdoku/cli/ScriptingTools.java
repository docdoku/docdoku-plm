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
package com.docdoku.cli;


import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.services.IUploadDownloadWS;
import com.docdoku.core.services.IWorkflowManagerWS;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public class ScriptingTools {

    public static String HTTP_CLIENT_STREAMING_CHUNK_SIZE;
    public final static String JAVA7_HTTP_CLIENT_STREAMING_CHUNK_SIZE = "com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size";
    public final static String JAVA6_HTTP_CLIENT_STREAMING_CHUNK_SIZE = "com.sun.xml.ws.transport.http.client.streaming.chunk.size";


    private final static String DOCUMENT_WSDL_LOCATION = "/services/document?wsdl";
    private final static String PRODUCT_WSDL_LOCATION = "/services/product?wsdl";
    private final static String WORKFLOW_WSDL_LOCATION = "/services/workflow?wsdl";
    private final static String FILE_MANAGER_WSDL_LOCATION = "/services/UploadDownload?wsdl";

    static {
        String version = System.getProperty("java.version"); 
        if (version.startsWith("1.7")) {
            HTTP_CLIENT_STREAMING_CHUNK_SIZE = JAVA7_HTTP_CLIENT_STREAMING_CHUNK_SIZE;
        } else {
            HTTP_CLIENT_STREAMING_CHUNK_SIZE = JAVA6_HTTP_CLIENT_STREAMING_CHUNK_SIZE;
        }
    }
    private ScriptingTools() {
        
    }

    public static IDocumentManagerWS createDocumentService(URL url, String login, String password) throws MalformedURLException, Exception {
        DocumentService service = new DocumentService(new URL(url, DOCUMENT_WSDL_LOCATION), new javax.xml.namespace.QName("http://server.docdoku.com/", "DocumentManagerBeanService"));
        IDocumentManagerWS port = service.getPort(IDocumentManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IProductManagerWS createProductService(URL url, String login, String password) throws MalformedURLException, Exception {
        ProductService service = new ProductService(new URL(url, PRODUCT_WSDL_LOCATION), new javax.xml.namespace.QName("http://server.docdoku.com/", "ProductManagerBeanService"));
        IProductManagerWS port = service.getPort(IProductManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IWorkflowManagerWS createWorkflowService(URL url, String login, String password) throws MalformedURLException, Exception {
        WorkflowService service = new WorkflowService(new URL(url, WORKFLOW_WSDL_LOCATION), new javax.xml.namespace.QName("http://server.docdoku.com/", "WorkflowManagerBeanService"));
        IWorkflowManagerWS port = service.getPort(IWorkflowManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IUploadDownloadWS createFileManagerService(URL url, String login, String password) throws MalformedURLException {
        MTOMFeature feature = new MTOMFeature();
        UploadDownloadService service = new UploadDownloadService(new URL(url, FILE_MANAGER_WSDL_LOCATION), new javax.xml.namespace.QName("http://server.docdoku.com/", "UploadDownloadService"));
        IUploadDownloadWS proxy = service.getPort(IUploadDownloadWS.class, feature);
        Map context = ((BindingProvider) proxy).getRequestContext();
        context.put(HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        context.put(BindingProvider.USERNAME_PROPERTY, login);
        context.put(BindingProvider.PASSWORD_PROPERTY, password);
        return proxy;
    }

}
