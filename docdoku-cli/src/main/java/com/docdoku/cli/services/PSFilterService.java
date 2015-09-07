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
 * @author Florent Garin
 */
@WebServiceClient(name = "PSFilterService", targetNamespace = "http://server.docdoku.com/", wsdlLocation = "http://localhost:8080/services/psFilter?wsdl")
public class PSFilterService extends Service{
    private static final URL PSFILTERSERVICE_WSDL_LOCATION;
    private static final QName PSFILTERSERVICE_QNAME = new QName("http://server.docdoku.com/", "ProductManagerBeanService");
    private static final Logger LOGGER = Logger.getLogger(PSFilterService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = PSFilterService.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:8080/services/psFilter?wsdl");
        } catch (MalformedURLException e) {
            LOGGER.warning("Failed to create URL for the wsdl Location: 'http://localhost:8080/services/psFilter?wsdl', retrying as a local file");
            LOGGER.warning(e.getMessage());
        }
        PSFILTERSERVICE_WSDL_LOCATION = url;
    }

    public PSFilterService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PSFilterService() {
        super(PSFILTERSERVICE_WSDL_LOCATION, PSFILTERSERVICE_QNAME);
    }

}