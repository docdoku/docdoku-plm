/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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
import java.util.UUID;

/**
 * Represents a password recovery request. This class makes the link between
 * the UUID of the request and the user who asked it.
 * 
 * @author Florent Garin
 * @version 1.0, 01/04/11
 * @since   V1.0
 */
@javax.persistence.Entity
public class PasswordRecoveryRequest implements java.io.Serializable {
    
    @javax.persistence.Id
    private String uuid="";
    
    private String login;
    
    
    public PasswordRecoveryRequest() {
    }
    

    public static PasswordRecoveryRequest createPasswordRecoveryRequest(String login){
        PasswordRecoveryRequest passwdRR = new PasswordRecoveryRequest();
        passwdRR.setLogin(login);
        passwdRR.setUuid(UUID.randomUUID().toString());
        return passwdRR;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    @Override
    public String toString() {
        return uuid;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof PasswordRecoveryRequest))
            return false;
        PasswordRecoveryRequest passwdRR = (PasswordRecoveryRequest) pObj;
        return passwdRR.uuid.equals(uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
