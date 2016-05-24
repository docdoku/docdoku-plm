package com.docdoku.server.jwt;

import com.docdoku.core.common.Account;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTokenFactory {

    private static final Logger LOGGER = Logger.getLogger(JWTokenFactory.class.getName());
    private static RsaJsonWebKey key;

    private JWTokenFactory() {
    }

    public static String createToken(Account account) {
        RsaJsonWebKey rsaJsonWebKey = produce();
        JwtClaims claims = new JwtClaims();
        claims.setSubject(account.getLogin());
        JsonWebSignature jsonWebSignature = new JsonWebSignature();
        jsonWebSignature.setPayload(claims.toJson());
        jsonWebSignature.setKey(rsaJsonWebKey.getPrivateKey());
        jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        String jwt = null;
        try {
            jwt = jsonWebSignature.getCompactSerialization();
        } catch (JoseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return jwt;
    }

    public static RsaJsonWebKey produce(){
        if(key == null){
            try {
                key = RsaJwkGenerator.generateJwk(2048);
            } catch (JoseException ex) {
               LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return key;
    }
}
