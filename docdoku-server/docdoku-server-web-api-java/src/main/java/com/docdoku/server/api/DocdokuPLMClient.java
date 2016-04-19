package com.docdoku.server.api;

import com.docdoku.server.api.client.ApiClient;
import okhttp3.Credentials;

import java.text.SimpleDateFormat;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMClient {

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

    // TODO : add auth configurations, HttpBasicAuth only supported
    private void createClient(String host, String username, String password, boolean debug) {
        client = new ApiClient();
        client.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        client.setBasePath(host);
        client.setDebugging(debug);
        String credentials = Credentials.basic(username, password);
        client.addDefaultHeader("Authorization", credentials);
    }

}
