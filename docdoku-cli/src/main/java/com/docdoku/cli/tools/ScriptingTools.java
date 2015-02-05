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

package com.docdoku.cli.tools;

import com.docdoku.cli.services.*;
import com.docdoku.core.services.*;

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

    public static final String HTTP_CLIENT_STREAMING_CHUNK_SIZE;
    public static final String JAVA7_HTTP_CLIENT_STREAMING_CHUNK_SIZE = "com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size";
    public static final String JAVA6_HTTP_CLIENT_STREAMING_CHUNK_SIZE = "com.sun.xml.ws.transport.http.client.streaming.chunk.size";


    private static final String DOCUMENT_WSDL_LOCATION = "/services/document?wsdl";
    private static final String PRODUCT_WSDL_LOCATION = "/services/product?wsdl";
    private static final String PRODUCT_CONFIGSPEC_WSDL_LOCATION = "/services/productConfigSpec?wsdl";
    private static final String WORKFLOW_WSDL_LOCATION = "/services/workflow?wsdl";
    private static final String USER_WSDL_LOCATION = "/services/user?wsdl";
    private static final String FILE_MANAGER_WSDL_LOCATION = "/services/UploadDownload?wsdl";

    private static final String NAMESPACEURI = "http://server.docdoku.com/";
    private static final String PRODUCT_NAMESPACEURI = "http://products.server.docdoku.com/";

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

    public static IDocumentManagerWS createDocumentService(URL url, String login, String password) throws Exception {
        DocumentService service = new DocumentService(new URL(url, DOCUMENT_WSDL_LOCATION), new javax.xml.namespace.QName(NAMESPACEURI, "DocumentManagerBeanService"));
        IDocumentManagerWS port = service.getPort(IDocumentManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IProductManagerWS createProductService(URL url, String login, String password) throws Exception {
        ProductService service = new ProductService(new URL(url, PRODUCT_WSDL_LOCATION), new javax.xml.namespace.QName(NAMESPACEURI, "ProductManagerBeanService"));
        IProductManagerWS port = service.getPort(IProductManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IProductConfigSpecManagerWS createProductConfigSpecService(URL url, String login, String password) throws Exception {
        ProductConfigSpecService service = new ProductConfigSpecService(new URL(url, PRODUCT_CONFIGSPEC_WSDL_LOCATION), new javax.xml.namespace.QName(PRODUCT_NAMESPACEURI, "ProductConfigSpecManagerBeanService"));
        IProductConfigSpecManagerWS port = service.getPort(IProductConfigSpecManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IWorkflowManagerWS createWorkflowService(URL url, String login, String password) throws Exception {
        WorkflowService service = new WorkflowService(new URL(url, WORKFLOW_WSDL_LOCATION), new javax.xml.namespace.QName(NAMESPACEURI, "WorkflowManagerBeanService"));
        IWorkflowManagerWS port = service.getPort(IWorkflowManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IUploadDownloadWS createFileManagerService(URL url, String login, String password) throws MalformedURLException {
        MTOMFeature feature = new MTOMFeature();
        UploadDownloadService service = new UploadDownloadService(new URL(url, FILE_MANAGER_WSDL_LOCATION), new javax.xml.namespace.QName(NAMESPACEURI, "UploadDownloadService"));
        IUploadDownloadWS proxy = service.getPort(IUploadDownloadWS.class, feature);
        Map context = ((BindingProvider) proxy).getRequestContext();
        context.put(HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        context.put(BindingProvider.USERNAME_PROPERTY, login);
        context.put(BindingProvider.PASSWORD_PROPERTY, password);
        return proxy;
    }


    public static IUserManagerWS createUserManagerService(URL url, String login, String password) throws MalformedURLException {
        UserService service = new UserService(new URL(url, USER_WSDL_LOCATION), new javax.xml.namespace.QName(NAMESPACEURI, "UserManagerBeanService"));
        IUserManagerWS port = service.getPort(IUserManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

}
