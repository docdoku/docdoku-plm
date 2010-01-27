/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.core.ICommandWS;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.ws.BindingProvider;

/**
 *
 * @author Florent GARIN
 */
public class ScriptingTools {
    
    private final static String DEFAULT_WSDL_LOCATION="http://localhost:8080/webservices/DocDoku?wsdl";

    
    
    private ScriptingTools(){}
    
    public static ICommandWS logToServer(String url, String login, String password, String workspace) throws MalformedURLException, Exception{
        CommandService service = new CommandService(new URL(url),new javax.xml.namespace.QName("http://server.docdoku.com/", "CommandBeanService"));
        ICommandWS port= service.getPort(ICommandWS.class);
        ((BindingProvider)port).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, login);
        ((BindingProvider)port).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password);
     
        return port;
    }
    
    public static ICommandWS logToServer(String login, String password, String workspace) throws MalformedURLException, Exception{
        return logToServer(DEFAULT_WSDL_LOCATION,login,password,workspace);
    }
    
}
