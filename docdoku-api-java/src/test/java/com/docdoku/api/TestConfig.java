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
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.services.AccountsApi;

/**
 * Static test config
 * Override any value from pom.xml / env args: -Denv.PARAM_NAME=PARAM_VALUE
 *
 * @Author Morgan Guimard
 */
public class TestConfig {

    public static String URL;
    public static String LOGIN;
    public static String PASSWORD;
    public static String ROOT_LOGIN;
    public static String ROOT_PASSWORD;
    public static String EMAIL;
    public static String NAME;
    public static String LANGUAGE;
    public static String TIMEZONE;
    public static boolean DEBUG;

    public static ApiClient GUEST_CLIENT;
    public static ApiClient BASIC_CLIENT;

    public static ApiClient JWT_CLIENT;
    public static ApiClient COOKIE_CLIENT;

    public static ApiClient ROOT_CLIENT;
    public static ApiClient REGULAR_USER_CLIENT;

    static {

        URL = System.getProperty("url");
        LOGIN = System.getProperty("login");
        NAME = System.getProperty("name");
        PASSWORD = System.getProperty("password");
        ROOT_PASSWORD = System.getProperty("root_password");
        ROOT_LOGIN = System.getProperty("root_login");
        EMAIL = System.getProperty("email");
        LANGUAGE = System.getProperty("language");
        TIMEZONE = System.getProperty("timezone");
        DEBUG = Boolean.valueOf(System.getProperty("debug"));

        GUEST_CLIENT = DocDokuPLMClientFactory.createClient(URL, DEBUG);
        BASIC_CLIENT = DocDokuPLMClientFactory.createBasicClient(URL, LOGIN, PASSWORD, DEBUG);
        COOKIE_CLIENT = DocDokuPLMClientFactory.createCookieClient(URL, LOGIN, PASSWORD, DEBUG);
        JWT_CLIENT = DocDokuPLMClientFactory.createJWTClient(URL, LOGIN, PASSWORD, DEBUG);
        ROOT_CLIENT = DocDokuPLMClientFactory.createJWTClient(URL, ROOT_LOGIN, ROOT_PASSWORD, DEBUG);
        REGULAR_USER_CLIENT = JWT_CLIENT;

        // init test account
        try {
            AccountsApi accountsApi = new AccountsApi(REGULAR_USER_CLIENT);
            AccountDTO account = accountsApi.getAccount();
            account.setEmail(EMAIL);
            account.setPassword(PASSWORD);
            accountsApi.updateAccount(account);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

}
