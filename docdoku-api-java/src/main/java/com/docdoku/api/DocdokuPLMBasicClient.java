package com.docdoku.api;

import com.docdoku.api.client.ApiClient;
import okhttp3.Credentials;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMBasicClient extends DocdokuPLMClient{

    public DocdokuPLMBasicClient(String host, String login, String password) {
        this(host,login,password,false);
    }

    public DocdokuPLMBasicClient(String host, String login, String password, boolean debug)  {
        super(host,debug);
        client.addDefaultHeader("Authorization", Credentials.basic(login, password));
    }

    public ApiClient getClient(){
        return client;
    }

}
