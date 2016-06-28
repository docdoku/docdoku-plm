package com.docdoku.api;

import com.docdoku.api.client.ApiClient;
import okhttp3.Credentials;

import java.util.logging.Logger;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMBasicClient extends DocdokuPLMClient{

    private String cookie;

    private static final Logger LOGGER = Logger.getLogger(DocdokuPLMBasicClient.class.getName());

    public DocdokuPLMBasicClient(String host, String login, String password) {
        this(host,login,password,false);
    }

    public DocdokuPLMBasicClient(String host, String login, String password, boolean debug)  {
        this.host = host;
        this.debug = debug;
        createClient();
        client.addDefaultHeader("Authorization", Credentials.basic(login, password));
    }

    public ApiClient getClient(){
        return client;
    }

}
