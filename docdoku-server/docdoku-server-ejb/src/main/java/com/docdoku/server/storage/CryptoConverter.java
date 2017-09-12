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

package com.docdoku.server.storage;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.naming.InitialContext;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final Logger LOGGER = Logger.getLogger(CryptoConverter.class.getName());
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private static Key key;

    //PostConstruct event not yet available on converters
    static {
        try {
            InitialContext ctx = new InitialContext();
            Properties props = (Properties) ctx.lookup("security.config");

            String keystoreLocation = props.getProperty("keystoreLocation");
            String keystorePass = props.getProperty("keystorePass");
            String keyAlias = props.getProperty("keyAlias");

            String keystoreType = Optional.ofNullable(props.getProperty("keystoreType")).orElse("JCEKS");
            String keyPass = Optional.ofNullable(props.getProperty("keyPass")).orElse(keystorePass);

            KeyStore ks = KeyStore.getInstance(keystoreType);
            try (InputStream fis = new BufferedInputStream(new FileInputStream(keystoreLocation))) {
                ks.load(fis, keystorePass.toCharArray());
            }
            key = ks.getKey(keyAlias, keyPass.toCharArray());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Keystore loading failed", ex);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attrValue) {
        
        if (attrValue == null || attrValue.isEmpty()) {
            return attrValue;
        }

        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, key);
            //retrieves the initialization vector which needs to be stored along the ciphered data
            byte[] iv = c.getIV();
            //the '.' is a safe delimiter for Base64 data
            String base64IV = Base64.getEncoder().encodeToString(iv);
            String base64EncryptedData = Base64.getEncoder().encodeToString(c.doFinal(attrValue.getBytes("UTF-8")));
            return base64IV + "." + base64EncryptedData;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot encrypt, stores the value unchanged", ex);
            return attrValue;
        }
    }

    @Override
    public String convertToEntityAttribute(String storedData) {
        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            String[] strData = storedData.split("\\.");
            byte[] iv = Base64.getDecoder().decode(strData[0]);
            c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            return new String(c.doFinal(Base64.getDecoder().decode(strData[1])), "UTF-8");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Cannot decrypt, returns the value as stored", ex);
            return storedData;
        }
    }
}