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

package com.docdoku.server.auth;

import org.jose4j.keys.HmacKey;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get auth config from resources
 *
 * @author Morgan Guimard
 */
@ApplicationScoped
public class AuthConfig {

    private Properties properties;
    private Key defaultKey;

    private static final Logger LOGGER = Logger.getLogger(AuthConfig.class.getName());


    @PostConstruct
    private void init() {
        try {
            InitialContext ctx = new InitialContext();
            properties = (Properties) ctx.lookup("auth.config");
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            defaultKey = keyGen.generateKey();
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Cannot initialize auth configuration", e);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Cannot generate random JWT default key", e);
        }
    }

    public Boolean isJwtEnabled() {
        return Boolean.parseBoolean(properties.getProperty("jwt.enabled"));
    }

    public Boolean isBasicHeaderEnabled() {
        return Boolean.parseBoolean(properties.getProperty("basic.header.enabled"));
    }

    public Boolean isSessionEnabled() {
        return Boolean.parseBoolean(properties.getProperty("session.enabled"));
    }

    public Key getJWTKey() {
        try {
            String secret = properties.getProperty("jwt.key");
            if (null != secret && !secret.isEmpty()) {
                return new HmacKey(secret.getBytes("UTF-8"));
            }
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Cannot create JWT key", e);
        }
        return defaultKey;
    }
}
