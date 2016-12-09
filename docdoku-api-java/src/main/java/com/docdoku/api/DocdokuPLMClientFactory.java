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
import com.docdoku.api.client.ApiResponse;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.models.LoginRequestDTO;
import com.docdoku.api.services.AuthApi;
import com.squareup.okhttp.Credentials;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * This class helps to create an ApiClient with several authentication methods
 *
 * @Author Morgan Guimard
 */
public class DocdokuPLMClientFactory {

    /**
     * Create a guest client, no debug
     **/
    public static ApiClient createClient(String host) {
        return createClient(host, false);
    }

    /**
     * Create a guest client, control debug
     **/
    public static ApiClient createClient(String host, boolean debug) {
        ApiClient client = new ApiClient();
        client.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        client.setBasePath(host);
        client.setDebugging(debug);
        return client;
    }

    /**
     * Create a basic client, no debug
     **/
    public static ApiClient createBasicClient(String host, String login, String password) {
        return createBasicClient(host, login, password, false);
    }

    /**
     * Create a basic client, control debug
     **/
    public static ApiClient createBasicClient(String host, String login, String password, boolean debug) {
        ApiClient client = createClient(host, debug);
        client.addDefaultHeader("Authorization", Credentials.basic(login, password));
        return client;
    }

    /**
     * Create a jwt client, no debug
     **/
    public static ApiClient createJWTClient(String host, String login, String password) {
        return createJWTClient(host, login, password);
    }

    /**
     * Create a jwt client, control debug
     **/
    public static ApiClient createJWTClient(String host, String login, String password, boolean debug) {

        ApiClient client = createClient(host, debug);

        try {
            ApiResponse<AccountDTO> response = connect(client, login, password);
            Map<String, List<String>> responseHeaders = response.getHeaders();
            String token = responseHeaders.get("jwt").get(0);
            client.addDefaultHeader("Authorization", "Bearer " + token);
        } catch (ApiException e) {

        }

        return client;
    }

    /**
     * Create a cookie client, no debug
     **/
    public static ApiClient createCookieClient(String host, String login, String password) {
        return createCookieClient(host, login, password, false);
    }

    /**
     * Create a cookie client, control debug
     **/
    public static ApiClient createCookieClient(String host, String login, String password, boolean debug) {
        ApiClient client = createClient(host, debug);

        try {
            ApiResponse<AccountDTO> response = connect(client, login, password);
            Map<String, List<String>> responseHeaders = response.getHeaders();
            String cookie = responseHeaders.get("Set-Cookie").get(0);
            client.addDefaultHeader("Cookie", cookie);
        } catch (ApiException e) {

        }

        return client;
    }

    private static ApiResponse<AccountDTO> connect(ApiClient client, String login, String password) throws ApiException {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLogin(login);
        loginRequest.setPassword(password);
        return new AuthApi(client).loginWithHttpInfo(loginRequest);
    }
}
