/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server.resourcegetters;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class OfficeConfig {

    private Properties properties;

    private static final Logger LOGGER = Logger.getLogger(OfficeConfig.class.getName());

    @PostConstruct
    private void init() {
        try {
            InitialContext ctx = new InitialContext();
            properties = (Properties) ctx.lookup("office.config");
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Cannot initialize office configuration", e);
        }
    }

    public String getOfficeHome() {
        return properties.getProperty("office_home");
    }

    public Integer getOfficePort() {
        return Integer.parseInt(properties.getProperty("office_port"));
    }
}
