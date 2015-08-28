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
package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IOrganizationManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.OrganizationDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Locale;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IOrganizationManagerLocal.class)
@Stateless(name = "OrganizationManagerBean")
public class OrganizationManagerBean implements IOrganizationManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IAccountManagerLocal accountManager;

    private static final Logger LOGGER = Logger.getLogger(UserManagerBean.class.getName());

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void updateOrganization(Organization pOrganization)
            throws AccountNotFoundException, OrganizationNotFoundException, AccessRightException {

        OrganizationDAO organizationDAO = new OrganizationDAO(em);
        Organization oldOrganization = organizationDAO.loadOrganization(pOrganization.getName());

        if (accountManager.checkAdmin(oldOrganization) != null) {
            organizationDAO.updateOrganization(pOrganization);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Organization createOrganization(String pName, Account pOwner, String pDescription) throws OrganizationAlreadyExistsException, CreationException, NotAllowedException {
        if (pOwner.getOrganization() == null) {
            Organization organization = new Organization(pName, pOwner, pDescription);
            new OrganizationDAO(new Locale(pOwner.getLanguage()), em).createOrganization(organization);
            pOwner.setOrganization(organization);
            organization.addMember(pOwner);
            em.merge(pOwner);
            return organization;
        } else {
            throw new NotAllowedException(new Locale(pOwner.getLanguage()), "NotAllowedException11");
        }
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    public void deleteOrganization(String pName) throws OrganizationNotFoundException, AccountNotFoundException, AccessRightException {
        OrganizationDAO organizationDAO = new OrganizationDAO(em);
        Organization organization = organizationDAO.loadOrganization(pName);

        if (accountManager.checkAdmin(organization) != null) {
            organizationDAO.deleteOrganization(organization);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void addAccountInOrganization(String pOrganizationName, String pLogin) throws OrganizationNotFoundException, AccountNotFoundException, AccessRightException, NotAllowedException {
        OrganizationDAO organizationDAO = new OrganizationDAO(em);
        Organization organization = organizationDAO.loadOrganization(pOrganizationName);
        Account account = accountManager.checkAdmin(organization);
        Locale locale;

        if (account != null) {
            locale = new Locale(account.getLanguage());
        } else {
            locale = Locale.getDefault();
        }

        Account accountToAdd = new AccountDAO(locale, em).loadAccount(pLogin);
        if (accountToAdd.getOrganization() != null) {
            throw new NotAllowedException(locale, "NotAllowedException12");
        } else {
            accountToAdd.setOrganization(organization);
            organization.addMember(accountToAdd);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void removeAccountsFromOrganization(String pOrganizationName, String[] pLogins) throws AccessRightException, OrganizationNotFoundException, AccountNotFoundException {
        OrganizationDAO organizationDAO = new OrganizationDAO(em);
        Organization organization = organizationDAO.loadOrganization(pOrganizationName);

        if (accountManager.checkAdmin(organization) != null) {
            for (String login : pLogins) {
                Account accountToRemove = new AccountDAO(em).loadAccount(login);
                accountToRemove.setOrganization(null);
                organization.removeMember(accountToRemove);
            }
        }
    }

}
