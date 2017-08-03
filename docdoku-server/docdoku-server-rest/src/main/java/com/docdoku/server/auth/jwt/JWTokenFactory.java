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
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This JWTokenFactory class is responsible for JWT tokens creation
 *
 * @author Morgan Guimard
 */
public class JWTokenFactory {

    private static final Logger LOGGER = Logger.getLogger(JWTokenFactory.class.getName());
    private static final String ALG = AlgorithmIdentifiers.HMAC_SHA256;
    private static final Long JWT_TOKEN_EXPIRES_TIME = 10 * 60l; // 10 minutes token lifetime
    private static final Long JWT_TOKEN_REFRESH_BEFORE = 3 * 60l; // Deliver new token 3 minutes before expiration

    private static final String SUBJECT_LOGIN = "login";
    private static final String SUBJECT_GROUP_NAME = "groupName";

    private JWTokenFactory() {
    }

    public static String createToken(Key key, UserGroupMapping userGroupMapping) {

        JwtClaims claims = new JwtClaims();

        JsonObjectBuilder subjectBuilder = Json.createObjectBuilder();
        subjectBuilder.add(SUBJECT_LOGIN, userGroupMapping.getLogin());
        subjectBuilder.add(SUBJECT_GROUP_NAME, userGroupMapping.getGroupName());
        JsonObject build = subjectBuilder.build();

        claims.setSubject(build.toString());
        claims.setExpirationTime(NumericDate.fromSeconds(NumericDate.now().getValue() + JWT_TOKEN_EXPIRES_TIME));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setAlgorithmHeaderValue(ALG);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static JWTokenUserGroupMapping validateToken(Key key, String jwt) {

        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKey(key)
                .setRelaxVerificationKeyValidation()
                .build();

        try {

            JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
            String subject = jwtClaims.getSubject();

            JsonReader reader = Json.createReader(new StringReader(subject));
            JsonObject subjectObject = reader.readObject(); // JsonParsingException
            String login = subjectObject.getString(SUBJECT_LOGIN); // Npe
            String groupName = subjectObject.getString(SUBJECT_GROUP_NAME); // Npe

            if (login != null && !login.isEmpty() && groupName != null && !groupName.isEmpty()) {
                return new JWTokenUserGroupMapping(jwtClaims, new UserGroupMapping(login, groupName));
            }

        } catch (InvalidJwtException | MalformedClaimException | JsonParsingException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Cannot validate jwt token", e);
        }

        return null;

    }

    public static void refreshTokenIfNeeded(Key key, HttpServletResponse response, JWTokenUserGroupMapping jwTokenUserGroupMapping) {

        try {
            NumericDate expirationTime = jwTokenUserGroupMapping.getClaims().getExpirationTime();

            if (NumericDate.now().getValue() + JWT_TOKEN_REFRESH_BEFORE >= expirationTime.getValue()) {
                UserGroupMapping userGroupMapping = jwTokenUserGroupMapping.getUserGroupMapping();
                response.addHeader("jwt", createToken(key, userGroupMapping));
            }

        } catch (MalformedClaimException e) {
            LOGGER.log(Level.SEVERE, "Cannot get expiration time from claims", e);
        }

    }
}
