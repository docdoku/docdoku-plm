package com.docdoku.server.jwt;

import com.docdoku.core.common.Account;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTokenFactory {

    private static final Logger LOGGER = Logger.getLogger(JWTokenFactory.class.getName());
    private static RsaJsonWebKey key;
    private static final String ALG = AlgorithmIdentifiers.RSA_USING_SHA256;

    private JWTokenFactory() {
    }

    public static String createToken(Account account) {

        RsaJsonWebKey rsaJsonWebKey = RsaJsonWebKeyFactory.createKey();

        JwtClaims claims = new JwtClaims();
        claims.setSubject(account.getLogin());

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setAlgorithmHeaderValue(ALG);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
