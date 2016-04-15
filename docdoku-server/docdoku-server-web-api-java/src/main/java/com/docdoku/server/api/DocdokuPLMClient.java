package com.docdoku.server.api;

import com.docdoku.server.api.client.ApiClient;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMClient {

    private static final Logger LOGGER = Logger.getLogger(DocdokuPLMClient.class.getName());

    private ApiClient client;

    public DocdokuPLMClient(String host, String login, String password) {
        createClient(host,login,password,false);
    }

    public DocdokuPLMClient(String host, String login, String password, boolean debug)  {
        createClient(host,login,password,debug);
    }

    public ApiClient getClient(){
        return client;
    }

    private void createClient(String host, String login, String password, boolean debug) {
        client = new ApiClient();
        client.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        client.setBasePath(host);
        client.setDebugging(debug);
        try{
            byte[] authorizationValue = (login + ":" + password).getBytes("ISO-8859-1");
            byte[] encoded = Base64.encodeBase64(authorizationValue);
            client.addDefaultHeader("Authorization", "Basic " + new String(encoded, "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,null,e);
        }

    }

}
