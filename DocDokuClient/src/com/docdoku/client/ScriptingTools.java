/*
 * ScriptingTools.java
 *
 * Created on 4 mars 2008, 23:41
 *
 */

package com.docdoku.client;

import com.docdoku.client.actions.MainController;
import com.docdoku.client.data.Config;
import com.docdoku.core.ICommandWS;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;

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
