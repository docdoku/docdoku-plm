/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.core.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Useful class for storing credential, login/password pair, to the persistence
 * storage. 
 * 
 * @author Florent GARIN
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.Entity
public class Credential implements java.io.Serializable {
    
    @javax.persistence.Id
    private String login="";
    
    private String password;
    
    
    public Credential() {
    }
    
    public static Credential createCredential(String pLogin, String pClearPassword){
        Credential credential = new Credential();
        credential.login = pLogin;
        try {
            credential.password=md5Sum(pClearPassword);
        } catch (NoSuchAlgorithmException pEx) {
            System.err.println(pEx.getMessage());
        }
        return credential;
    }
    
    private static String md5Sum(String pText) throws NoSuchAlgorithmException{
        byte[] digest = MessageDigest.getInstance("MD5").digest(pText.getBytes());
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if (hex.length() == 1) {
                hexString.append("0" + hex);
            } else {
                hexString.append(hex);
            }
        }
        return hexString.toString();
    }
}
