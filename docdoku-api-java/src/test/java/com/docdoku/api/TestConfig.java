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
