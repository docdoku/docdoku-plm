/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps to create an ApiClient with several authentication methods
 *
 * @Author Morgan Guimard
 */
public class DocDokuPLMClientFactory {

    private static final Logger LOGGER = Logger.getLogger(DocDokuPLMClientFactory.class.getName());

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
     * <p>
     * Soon to be deprecated, consider using createJWTClient instead
     **/
    public static ApiClient createBasicClient(String host, String login, String password) {
        return createBasicClient(host, login, password, false);
    }

    /**
     * Create a basic client, control debug
     * <p>
     * Soon to be deprecated, consider using createJWTClient instead
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
        return createJWTClient(host, login, password, false);
    }

    /**
     * Create a jwt client, control debug
     **/
    public static ApiClient createJWTClient(String host, String login, String password, boolean debug) {

        final ApiClient client = createClient(host, debug);

        client.getHttpClient().networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                String jwt = response.header("jwt");
                if (jwt != null && !jwt.isEmpty()) {
                    LOGGER.log(Level.FINEST, "JWT token received " + jwt);
                    client.addDefaultHeader("Authorization", "Bearer " + jwt);
                }
                return response;
            }
        });

        try {
            connect(client, login, password);
            LOGGER.log(Level.FINEST, "Connected");
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Exception while trying to get a token", e);
        }

        return client;
    }

    /**
     * Create a jwt client from host and token, no debug
     **/
    public static ApiClient createJWTClient(String host, String token) {
        return createJWTClient(host,token,false);
    }

    /**
     * Create a jwt client from host and token, debug control
     **/
    public static ApiClient createJWTClient(String host, String token, boolean debug) {
        final ApiClient client = createClient(host, debug);
        client.addDefaultHeader("Authorization", "Bearer " + token);
        return client;
    }


    /**
     * Create a cookie client, no debug
     *
     * Soon to be deprecated, consider using createJWTClient instead
     **/
    public static ApiClient createCookieClient(String host, String login, String password) {
        return createCookieClient(host, login, password, false);
    }

    /**
     * Create a cookie client, control debug
     *
     * Soon to be deprecated, consider using createJWTClient instead
     **/
    public static ApiClient createCookieClient(String host, String login, String password, boolean debug) {
        final ApiClient client = createClient(host, debug);

        client.getHttpClient().networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                String cookie = response.header("Set-Cookie");
                if (cookie != null && !cookie.isEmpty()) {
                    LOGGER.log(Level.FINEST, "Cookie received " + cookie);
                    client.addDefaultHeader("Cookie", cookie);
                }
                return response;
            }
        });

        try {
            connect(client, login, password);
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Exception while trying to get a cookie", e);
        }

        return client;
    }

    private static ApiResponse<AccountDTO> connect(ApiClient client, String login, String password) throws ApiException {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setLogin(login);
        loginRequest.setPassword(password);
        return new AuthApi(client).loginWithHttpInfo(loginRequest);
    }

    public static String getTokenPayload(String token) {
        String[] jwtParts = token.split("\\.");
        return new String(base64UrlDecode(jwtParts[1]));
    }

    public static byte[] base64UrlDecode(String input) {
        return Base64.getDecoder().decode(input);
    }
}
