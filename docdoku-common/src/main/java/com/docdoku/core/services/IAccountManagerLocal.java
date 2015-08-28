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
package com.docdoku.core.services;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.exceptions.*;

/**
 *
 * @author Elisabel Généreux
 */
public interface IAccountManagerLocal {

    /**
     * Get the account matching a login. ONLY USE IN LOCAL.
     * @param pLogin Login you search
     * @return The account matching the login
     * @throws AccountNotFoundException If no account was found
     */
    Account getAccount(String pLogin) throws AccountNotFoundException;

    Account createAccount(String pLogin, String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountAlreadyExistsException, CreationException;
    void updateAccount(String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountNotFoundException;

    Account getMyAccount() throws AccountNotFoundException;

    Account checkAdmin(Organization pOrganization) throws AccessRightException, AccountNotFoundException;
    Account checkAdmin(String pOrganizationName) throws AccessRightException, AccountNotFoundException, OrganizationNotFoundException;

    void setGCMAccount(String gcmId) throws AccountNotFoundException, GCMAccountAlreadyExistsException, CreationException;
    void deleteGCMAccount() throws AccountNotFoundException, GCMAccountNotFoundException;

}
