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



import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;


/**
 *
 * @author Florent GARIN
 */
@WebServiceClient(name = "DocumentService", targetNamespace = "http://server.docdoku.com/", wsdlLocation = "http://localhost:8080/services/document?wsdl")
public class DocumentService extends Service
{

    private final static URL DOCUMENTSERVICE_WSDL_LOCATION;
    private final static QName DOCUMENTSERVICE_QNAME = new QName("http://server.docdoku.com/", "DocumentManagerBeanService");
    private final static Logger LOGGER = Logger.getLogger(com.docdoku.client.DocumentService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.docdoku.client.DocumentService.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:8080/services/document?wsdl");
        } catch (MalformedURLException e) {
            LOGGER.warning("Failed to create URL for the wsdl Location: 'http://localhost:8080/services/document?wsdl', retrying as a local file");
            LOGGER.warning(e.getMessage());
        }
        DOCUMENTSERVICE_WSDL_LOCATION = url;
    }

    public DocumentService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DocumentService() {
        super(DOCUMENTSERVICE_WSDL_LOCATION, DOCUMENTSERVICE_QNAME);
    }

    

}