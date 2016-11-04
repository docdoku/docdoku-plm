/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.api;

import com.docdoku.api.client.ApiClient;
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
 *
 * @author Morgan Guimard
 */
public class DocdokuPLMCookieClient extends DocdokuPLMClient {

    private String cookie;

    private static final Logger LOGGER = Logger.getLogger(DocdokuPLMCookieClient.class.getName());

    public DocdokuPLMCookieClient(String host, String login, String password) {
        this(host, login, password, false);
    }

    public DocdokuPLMCookieClient(String host, String login, String password, boolean debug) {
        super(host, debug);
        connect(login, password);
    }

    public void connect(String login, String password) {

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLogin(login);
        loginRequest.setPassword(password);

        try {
            AccountDTO account = new AuthApi(client).login(loginRequest);
            LOGGER.log(Level.INFO, "Connected as  " + account.getLogin());
            Map<String, List<String>> responseHeaders = client.getResponseHeaders();
            System.out.println(responseHeaders);
            List<String> strings = responseHeaders.get("Set-Cookie");
            if (strings != null && !strings.isEmpty()) {
                this.cookie = strings.get(0);
                createClient();
                client.addDefaultHeader("Cookie", this.cookie);
            } else {
                LOGGER.log(Level.WARNING, "Cannot fetch cookie");
            }

        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Cannot connect to docdoku plm server http response code = " + client.getStatusCode());
        }
    }

    public ApiClient getClient() {
        return client;
    }

}
