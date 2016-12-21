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

package com.docdoku.server.auth.jwt;

import com.docdoku.core.security.UserGroupMapping;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This JWTokenFactory class is responsible for JWT tokens creation
 *
 * @author Morgan Guimard
 */
public class JWTokenFactory {

    private static final Logger LOGGER = Logger.getLogger(JWTokenFactory.class.getName());
    private static final String ALG = AlgorithmIdentifiers.RSA_USING_SHA256;

    private JWTokenFactory() {
    }

    public static String createToken(UserGroupMapping userGroupMapping) {

        RsaJsonWebKey rsaJsonWebKey = RsaJsonWebKeyFactory.createKey();

        JwtClaims claims = new JwtClaims();
        claims.setSubject(userGroupMapping.getLogin() + ":" + userGroupMapping.getGroupName());

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

    public static UserGroupMapping validateToken(String jwt) {

        RsaJsonWebKey rsaJsonWebKey = RsaJsonWebKeyFactory.createKey();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireSubject()
                .setVerificationKey(rsaJsonWebKey.getKey())
                .build();

        String subject = null;

        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            subject = jwtClaims.getSubject();
        } catch (InvalidJwtException | MalformedClaimException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        if (subject != null && !subject.isEmpty()) {
            String[] loginAndRole = subject.split(":");
            if (loginAndRole.length == 2) {
                return new UserGroupMapping(loginAndRole[0], loginAndRole[1]);
            }
        }

        return null;

    }

}
