package com.docdoku.server.helpers;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *@author morgan
 */
public class Streams {

    private static final Logger LOGGER = Logger.getLogger(Streams.class.getName());

    public static void close(Closeable closeable){
        if(closeable != null){
            try {
                closeable.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
    }
}
