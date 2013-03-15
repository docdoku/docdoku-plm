package com.docdoku.test.smoke;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 14/03/13
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
public class SmokeTestProperties {
    private Properties properties;

    public SmokeTestProperties() {
        properties = new Properties();
        load();
    }

    public String getWebappURL(){
      return properties.getProperty("webapp.url");
    }

    public int getWebappPort(){
        return Integer.parseInt(properties.getProperty("webapp.port")) ;
    }

    public String getLoginForUser1(){
        return properties.getProperty("login.user1") ;
    }
    public String getLoginForUser2(){

        return properties.getProperty("login.user2") ;
    }
    public String getPassword(){
        return properties.getProperty("password");
    }
    public String getWorkspace(){
        return properties.getProperty("workspace");
    }
    public String getServerMailAddress(){
        return  properties.getProperty("server.mail.address");
    }
    public URL getURL() throws MalformedURLException {
        return new URL("http",this.getWebappURL(), this.getWebappPort(), "");
    }
    private Properties load() {
        try {
            properties.load(getClass().getResourceAsStream("/smoketest.properties"));
        }
        catch (IOException e) {
            throw new RuntimeException("smoketest.properties not found. Error: ", e);
        }
        return properties;
    }
}
