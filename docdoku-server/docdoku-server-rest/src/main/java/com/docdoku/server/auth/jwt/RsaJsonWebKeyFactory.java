/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class generate a RSA key
 *
 * @author Morgan Guimard
 */
public class RsaJsonWebKeyFactory {

    private static final Logger LOGGER = Logger.getLogger(RsaJsonWebKeyFactory.class.getName());
    private static final int KEY_SIZE = 2048;
    private static RsaJsonWebKey key;

    private RsaJsonWebKeyFactory() {
    }

    public static RsaJsonWebKey createKey() {
        if (key == null) {
            try {
                key = RsaJwkGenerator.generateJwk(KEY_SIZE);
            } catch (JoseException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return key;
    }

}
