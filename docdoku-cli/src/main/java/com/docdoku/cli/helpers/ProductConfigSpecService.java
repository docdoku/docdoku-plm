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

package com.docdoku.cli.helpers;

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
@WebServiceClient(name = "ProductConfigSpecService", targetNamespace = "http://server.docdoku.com/", wsdlLocation = "http://localhost:8080/services/productConfigSpec?wsdl")
public class ProductConfigSpecService extends Service
{

    private final static URL PRODUCTCONFIGSPECSERVICE_WSDL_LOCATION;
    private final static QName PRODUCTCONFIGSPECSERVICE_QNAME = new QName("http://server.docdoku.com/", "ProductConfigSpecManagerBeanService");
    private final static Logger LOGGER = Logger.getLogger(ProductConfigSpecService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = ProductConfigSpecService.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:8080/services/productConfigSpec?wsdl");
        } catch (MalformedURLException e) {
            LOGGER.warning("Failed to create URL for the wsdl Location: 'http://localhost:8080/services/productConfigSpec?wsdl', retrying as a local file");
            LOGGER.warning(e.getMessage());
        }
        PRODUCTCONFIGSPECSERVICE_WSDL_LOCATION = url;
    }

    public ProductConfigSpecService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ProductConfigSpecService() {
        super(PRODUCTCONFIGSPECSERVICE_WSDL_LOCATION, PRODUCTCONFIGSPECSERVICE_QNAME);
    }

    

}