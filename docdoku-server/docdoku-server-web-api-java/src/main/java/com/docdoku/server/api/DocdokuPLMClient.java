package com.docdoku.server.api;

import com.docdoku.server.api.client.ApiClient;
import com.docdoku.server.api.client.ApiException;
import com.docdoku.server.api.models.AccountDTO;
import com.docdoku.server.api.services.AccountsApi;
import okhttp3.Credentials;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMClient {

    private ApiClient client;
    private String host;
    private String cookie;
    private boolean debug;

    private static final Logger LOGGER = Logger.getLogger(DocdokuPLMClient.class.getName());

    public DocdokuPLMClient(String host, String login, String password) {
        this(host,login,password,false);
    }

    public DocdokuPLMClient(String host, String login, String password, boolean debug)  {
        this.host = host;
        this.debug = debug;
        createClient();
        connect(login, password);
    }

    public void connect(String login, String password){

        client.addDefaultHeader("Authorization",  Credentials.basic(login, password));

        try {
            AccountDTO account = new AccountsApi(client).getAccount();
            LOGGER.log(Level.INFO,"Connected as  " + account.getName());
            Map<String, List<String>> responseHeaders = client.getResponseHeaders();
            System.out.println(responseHeaders);
            List<String> strings = responseHeaders.get("Set-Cookie");
            if(strings != null && !strings.isEmpty()){
                this.cookie = strings.get(0);
                createClient();
                client.addDefaultHeader("Cookie", this.cookie);
            } else {
                LOGGER.log(Level.WARNING,"Cannot fetch cookie");
            }

        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE,"Cannot connect to docdoku plm server http response code = " + client.getStatusCode());        }
    }

    public ApiClient getClient(){
        return client;
    }

    private void createClient() {
        client = new ApiClient();
        client.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        client.setBasePath(host);
        client.setDebugging(debug);
    }

}
