/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
package com.docdoku.loaders;

import com.docdoku.cli.services.ProductService;
import com.docdoku.cli.services.UploadDownloadService;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.services.IUploadDownloadWS;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 *
 * @author Florent GARIN
 */
public class ScriptingTools {
    public static final String HTTP_CLIENT_STREAMING_CHUNK_SIZE= "com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size";

    private ScriptingTools() {
    }

    public static IProductManagerWS createProductService(String url, String login, String password) throws Exception {
        ProductService service = new ProductService(new URL(url), new javax.xml.namespace.QName("http://server.docdoku.com/", "ProductManagerBeanService"));
        IProductManagerWS port = service.getPort(IProductManagerWS.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);

        return port;
    }

    public static IUploadDownloadWS createFileManagerService(String url, String login, String password) throws MalformedURLException {
        MTOMFeature feature = new MTOMFeature();
        UploadDownloadService service = new UploadDownloadService(new URL(url), new javax.xml.namespace.QName("http://server.docdoku.com/", "UploadDownloadService"));
        IUploadDownloadWS proxy = service.getPort(IUploadDownloadWS.class, feature);
        Map context = ((BindingProvider) proxy).getRequestContext();
        context.put(HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        context.put(BindingProvider.USERNAME_PROPERTY, login);
        context.put(BindingProvider.PASSWORD_PROPERTY, password);
        return proxy;
    }
}