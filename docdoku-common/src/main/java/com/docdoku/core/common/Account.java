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

package com.docdoku.core.common;

import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Date;

/**
 * The Account class holds personal user data applicable inside the whole application.
 * However {@link User} objects encapsulate personal information
 * only in the context of a particular workspace.
 *
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since V1.0
 */
@Table(name="ACCOUNT")
@javax.persistence.Entity
public class Account implements Serializable, Cloneable {

    @javax.persistence.Id
    private String login="";

    private String name;
    private String email;
    private String language;
    private String timeZone = "Europe/London";

    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date creationDate;

    public Account(){
    }

    public Account(String pLogin, String pName, String pEmail, String pLanguage, Date pCreationDate,String pTimeZone) {
        login = pLogin;
        name = pName;
        email = pEmail;
        language = pLanguage;
        creationDate = pCreationDate;
        timeZone = pTimeZone;
    }

    public String getLogin() {
        return login;
    }
    public void setLogin(String pLogin) {
        login=pLogin;
    }

    public String getName() {
        return name;
    }
    public void setName(String pName) {
        name = pName;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }
    public String getEmail() {
        return email;
    }

    public void setLanguage(String pLanguage) {
        language = pLanguage;
    }
    public String getLanguage() {
        return language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setCreationDate(Date pCreationDate) {
        creationDate = (pCreationDate!=null) ? (Date) pCreationDate.clone() : null;
    }
    public Date getCreationDate() {
        return (creationDate!=null) ? (Date) creationDate.clone() : null;
    }

    @Override
    public String toString() {
        return login;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Account)){
            return false;
        }
        Account account = (Account) pObj;
        return account.login.equals(login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    /**
     * perform a deep clone operation
     */
    @Override
    public Account clone() {
        Account clone;
        try {
            clone = (Account) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        clone.creationDate = (Date) creationDate.clone();
        return clone;
    }
}