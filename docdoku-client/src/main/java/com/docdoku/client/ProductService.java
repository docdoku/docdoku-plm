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
@WebServiceClient(name = "ProductService", targetNamespace = "http://server.docdoku.com/", wsdlLocation = "http://localhost:8080/services/product?wsdl")
public class ProductService extends Service
{

    private final static URL PRODUCTSERVICE_WSDL_LOCATION;
    private final static QName PRODUCTSERVICE_QNAME = new QName("http://server.docdoku.com/", "ProductManagerBeanService");
    private final static Logger LOGGER = Logger.getLogger(com.docdoku.client.ProductService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.docdoku.client.ProductService.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:8080/services/product?wsdl");
        } catch (MalformedURLException e) {
            LOGGER.warning("Failed to create URL for the wsdl Location: 'http://localhost:8080/services/product?wsdl', retrying as a local file");
            LOGGER.warning(e.getMessage());
        }
        PRODUCTSERVICE_WSDL_LOCATION = url;
    }

    public ProductService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ProductService() {
        super(PRODUCTSERVICE_WSDL_LOCATION, PRODUCTSERVICE_QNAME);
    }

    

}