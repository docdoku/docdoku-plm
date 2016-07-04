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

public class TestConfig {
    public static final String URL = "http://localhost:8080/api";
    public static final String LOGIN = "foo";
    public static final String PASSWORD = "bar";
    public static final String WORKSPACE = "foo";
    public static final boolean DEBUG = true;
    public static final ApiClient GUEST_CLIENT = new DocdokuPLMClient(URL,DEBUG).getClient();
    public static final ApiClient BASIC_CLIENT = new DocdokuPLMBasicClient(URL,LOGIN, PASSWORD, DEBUG).getClient();
    public static final ApiClient JWT_CLIENT  = new DocdokuPLMBasicClient(URL, LOGIN, PASSWORD, DEBUG).getClient();
    public static final ApiClient COOKIE_CLIENT  = new DocdokuPLMCookieClient(URL, LOGIN, PASSWORD, DEBUG).getClient();
}
