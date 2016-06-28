package com.docdoku.server.example.utils;

import com.docdoku.api.client.ApiClient;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps debugging API errors
 * @author Morgan Guimard
 */
public class ErrorHelper {

    private static final Logger LOGGER = Logger.getLogger(ErrorHelper.class.getName());

    public static void onError(String message, ApiClient client){
        List<String> reason = client.getResponseHeaders().get("Reason-Phrase");
        LOGGER.log(Level.SEVERE, message + " [code="+client.getStatusCode()+"] \nReason : " + reason.get(0));
    }

}
