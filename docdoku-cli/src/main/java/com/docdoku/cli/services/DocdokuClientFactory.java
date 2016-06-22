package com.docdoku.cli.services;

import com.docdoku.server.api.DocdokuPLMBasicClient;
import com.docdoku.server.api.client.ApiClient;

public class DocdokuClientFactory {

    public static ApiClient createClient(String path, String user, String password){
        return new DocdokuPLMBasicClient(path, user, password).getClient();
    }
}


