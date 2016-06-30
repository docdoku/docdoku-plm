package com.docdoku.api;

import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.services.AccountsApi;
import okhttp3.Credentials;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMCookieClient extends DocdokuPLMClient{

    private String cookie;

    private static final Logger LOGGER = Logger.getLogger(DocdokuPLMCookieClient.class.getName());

    public DocdokuPLMCookieClient(String host, String login, String password) {
        this(host,login,password,false);
    }

    public DocdokuPLMCookieClient(String host, String login, String password, boolean debug)  {
        super(host,debug);
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

}
