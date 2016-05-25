package com.docdoku.server.jwt;


import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RsaJsonWebKeyFactory {

    private static final Logger LOGGER = Logger.getLogger(RsaJsonWebKeyFactory.class.getName());
    private static final int KEY_SIZE = 2048;
    private static RsaJsonWebKey key;

    private RsaJsonWebKeyFactory() {
    }

    public static RsaJsonWebKey createKey(){
        if(key == null){
            try {
                key = RsaJwkGenerator.generateJwk(KEY_SIZE);
            } catch (JoseException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return key;
    }

}
