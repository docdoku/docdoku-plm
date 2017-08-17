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

package com.docdoku.core.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Morgan Guimard
 */
public class HashUtils {
    public static String md5Sum(String pText) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return digest(pText, "MD5");
    }

    public static String sha256Sum(String pText) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return digest(pText,"SHA-256");
    }

    /**
     * Computes a hash function using the supplied algorithm and s
     * the result as a string representation using
     * hex as the encoding and UTF-8 as the character set.
     *
     * @param pText
     * @param pAlgorithm
     *
     * @return hashed string result.
     *
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String digest(String pText, String pAlgorithm) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] digest = MessageDigest.getInstance(pAlgorithm).digest(pText.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte aDigest : digest) {
            String hex = Integer.toHexString(0xFF & aDigest);
            if (hex.length() == 1) {
                hexString.append("0").append(hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();
    }
}
