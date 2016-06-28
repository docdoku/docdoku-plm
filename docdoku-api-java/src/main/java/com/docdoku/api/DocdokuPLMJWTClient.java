package com.docdoku.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.models.LoginRequestDTO;
import com.docdoku.api.services.AuthApi;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps to create the swagger client.
 * @Author Morgan Guimard
 */
public class DocdokuPLMJWTClient extends DocdokuPLMClient{

    private String token;

    private static final Logger LOGGER = Logger.getLogger(DocdokuPLMJWTClient.class.getName());

    public DocdokuPLMJWTClient(String host, String login, String password) {
        this(host,login,password,false);
    }

    public DocdokuPLMJWTClient(String host, String login, String password, boolean debug)  {
        this.host = host;
        this.debug = debug;
        createClient();
        connect(login, password);
    }

    @Override
    public void connect(String login, String password){

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setLogin(login);
        loginRequestDTO.setPassword(password);

        try {
            AccountDTO account = new AuthApi(client).login(loginRequestDTO);
            LOGGER.log(Level.INFO,"Connected as  " + account.getName());
            Map<String, List<String>> responseHeaders = client.getResponseHeaders();
            System.out.println(responseHeaders);
            List<String> strings = responseHeaders.get("jwt");
            if(strings != null && !strings.isEmpty()){
                this.token = strings.get(0);
                createClient();
                client.addDefaultHeader("Authorization", "Bearer " + this.token);
            } else {
                LOGGER.log(Level.WARNING,"Cannot fetch token");
            }

        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE,"Cannot connect to docdoku plm server http response code = " + client.getStatusCode());
        }

    }

}
