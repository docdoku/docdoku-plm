package com.docdoku.server.example.api;


import com.docdoku.server.api.DocdokuPLMClient;

/**
 * This class is a helper class to create some API calls tests
 * @Author Morgan Guimard
 */
public abstract class DocdokuPLMApiExample {

    protected DocdokuPLMClient plmClient;
    protected final static String WORKSPACE = "foo";
    private final static String API_URL = "http://localhost:8080/api";
    private final static String USERNAME = "foo";
    private final static String PASSWORD = "bar";
    private final static boolean DEBUG = false;

    public DocdokuPLMApiExample() {
        plmClient = new DocdokuPLMClient(API_URL,USERNAME,PASSWORD,DEBUG);
    }

    public abstract void run();
}
