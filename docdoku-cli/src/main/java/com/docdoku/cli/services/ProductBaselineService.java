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

package com.docdoku.cli.services;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 *
 * @author Taylor LABEJOF
 */
@WebServiceClient(name = "ProductBaselineService", targetNamespace = "http://server.docdoku.com/", wsdlLocation = "http://localhost:8080/services/productBaseline?wsdl")
public class ProductBaselineService extends Service{
    
    private static final URL PRODUCTBASELINESERVICE_WSDL_LOCATION;
    private static final QName PRODUCTBASELINESERVICE_QNAME = new QName("http://server.docdoku.com/", "ProductBaselineManagerBeanService");
    private static final Logger LOGGER = Logger.getLogger(ProductBaselineService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = ProductBaselineService.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:8080/services/productBaseline?wsdl");
        } catch (MalformedURLException e) {
            LOGGER.warning("Failed to create URL for the wsdl Location: 'http://localhost:8080/services/productBaseline?wsdl', retrying as a local file");
            LOGGER.warning(e.getMessage());
        }
        PRODUCTBASELINESERVICE_WSDL_LOCATION = url;
    }

    public ProductBaselineService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ProductBaselineService() {
        super(PRODUCTBASELINESERVICE_WSDL_LOCATION, PRODUCTBASELINESERVICE_QNAME);
    }
}