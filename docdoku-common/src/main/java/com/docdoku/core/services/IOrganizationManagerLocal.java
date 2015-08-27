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
public interface IOrganizationManagerLocal {

    Organization createOrganization(String pName, Account pOwner, String pDescription) throws OrganizationAlreadyExistsException, CreationException, NotAllowedException;
    void deleteOrganization(String pName) throws OrganizationNotFoundException, AccountNotFoundException, AccessRightException;
    void updateOrganization(Organization pOrganization) throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException;
    void addAccountInOrganization(String pOrganizationName, String pLogin) throws OrganizationNotFoundException, AccountNotFoundException, AccessRightException, NotAllowedException;
    void removeAccountsFromOrganization(String pOrganizationName, String[] pLogins) throws AccessRightException, OrganizationNotFoundException, AccountNotFoundException;

}
