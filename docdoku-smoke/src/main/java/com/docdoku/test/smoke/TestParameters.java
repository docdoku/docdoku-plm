package com.docdoku.test.smoke;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static junit.framework.TestCase.fail;

/**
 * Created with IntelliJ IDEA.
 * User: asmaechadid
 * Date: 11/03/13
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
public  class TestParameters {

    private  String serverAddress ;
    private  int serverPort ;
    private  String loginUser1 ;
    private  String loginUser2 ;
    private  String password ;
    private  String workspace ;
    private  String serverMailServer ;

    public TestParameters(){

        InputStream input =getClass().getClassLoader().getResourceAsStream("smoketest.properties");
        Properties p = new Properties();
        try {
            p.load(input);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        this.serverAddress = p.getProperty("ServerAddress");
        this.serverPort = Integer.parseInt(p.getProperty("ServerPort"));
        this.loginUser1 =  p.getProperty("loginUser1");
        this.loginUser2 =  p.getProperty("loginUser2");
        this.password = p.getProperty("password");
        this.workspace = p.getProperty("workspace");
        this.serverMailServer = p.getProperty("mailServer");
    }


    public  URL getURL() throws MalformedURLException {
        return new URL("http",  this.serverAddress, this.serverPort, "");
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getLoginUser1() {
        return loginUser1;
    }

    public String getLoginUser2() {
        return loginUser2;
    }

    public String getPassword() {
        return password;
    }

    public String getWorkspace() {
        return workspace;
    }


    public String getServerMailServer() {
        return serverMailServer;
    }


}
