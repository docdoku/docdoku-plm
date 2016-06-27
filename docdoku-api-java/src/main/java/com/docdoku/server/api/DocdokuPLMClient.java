package com.docdoku.server.api;

import com.docdoku.server.api.client.ApiClient;

import java.text.SimpleDateFormat;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMClient {

    protected ApiClient client;
    protected String host;
    protected boolean debug;

    public void connect(String login, String password){}

    protected void createClient() {
        client = new ApiClient();
        client.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        client.setBasePath(host);
        client.setDebugging(debug);
    }

    public ApiClient getClient() {
        return client;
    }
}
