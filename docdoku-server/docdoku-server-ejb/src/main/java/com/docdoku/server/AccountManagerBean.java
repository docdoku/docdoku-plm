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
import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IAccountManagerWS;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.GCMAccountDAO;
import com.docdoku.server.dao.OrganizationDAO;
import com.docdoku.server.dao.UserDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IAccountManagerLocal.class)
@Stateless(name = "AccountManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IAccountManagerWS")
public class AccountManagerBean implements IAccountManagerLocal, IAccountManagerWS {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;

    @EJB
    private IMailerLocal mailer;

    private static final Logger LOGGER = Logger.getLogger(UserManagerBean.class.getName());

    @Override
    public Account createAccount(String pLogin, String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountAlreadyExistsException, CreationException {
        Date now = new Date();
        Account account = new Account(pLogin, pName, pEmail, pLanguage, now, pTimeZone);
        new AccountDAO(new Locale(pLanguage), em).createAccount(account, pPassword);
        mailer.sendCredential(account);
        return account;
    }

    @Override
    public Account getAccount(String pLogin) throws AccountNotFoundException {
        return new AccountDAO(em).loadAccount(pLogin);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void updateAccount(String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountNotFoundException {
        AccountDAO accountDAO = new AccountDAO(new Locale(pLanguage), em);
        Account account = accountDAO.loadAccount(userManager.getCallerPrincipalLogin());
        account.setName(pName);
        account.setEmail(pEmail);
        account.setLanguage(pLanguage);
        account.setTimeZone(pTimeZone);
        if (pPassword != null) {
            accountDAO.updateCredential(account.getLogin(), pPassword);
        }

        // Sync user data in workspaces
        UserDAO userDAO = new UserDAO(new Locale(pLanguage), em);
        User[] users = userDAO.getUsers(account.getLogin());

        for (User user : users) {
            user.setEmail(pEmail);
            user.setLanguage(pLanguage);
            user.setName(pName);
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(Organization pOrganization) throws AccessRightException, AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(userManager.getCallerPrincipalLogin());

        if (!userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) && !pOrganization.getOwner().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }

        return account;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(String pOrganizationName)
            throws AccessRightException, AccountNotFoundException, OrganizationNotFoundException {

        Account account = new AccountDAO(em).loadAccount(userManager.getCallerPrincipalLogin());
        Organization organization = new OrganizationDAO(em).loadOrganization(pOrganizationName);

        if (!userManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) && !organization.getOwner().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }

        return account;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void setGCMAccount(String gcmId) throws AccountNotFoundException, GCMAccountAlreadyExistsException, CreationException {
        String callerLogin = userManager.getCallerPrincipalLogin();
        Account account = getAccount(callerLogin);
        GCMAccountDAO gcmAccountDAO = new GCMAccountDAO(em);

        try {
            GCMAccount gcmAccount = gcmAccountDAO.loadGCMAccount(account);
            gcmAccount.setGcmId(gcmId);
        } catch (GCMAccountNotFoundException e) {
            gcmAccountDAO.createGCMAccount(new GCMAccount(account, gcmId));
        }

    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteGCMAccount() throws AccountNotFoundException, GCMAccountNotFoundException {
        String callerLogin = userManager.getCallerPrincipalLogin();
        Account account = getAccount(callerLogin);
        GCMAccountDAO gcmAccountDAO = new GCMAccountDAO(new Locale(account.getLanguage()), em);
        GCMAccount gcmAccount = gcmAccountDAO.loadGCMAccount(account);
        gcmAccountDAO.deleteGCMAccount(gcmAccount);
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    @Override
    public Account getMyAccount() throws AccountNotFoundException {
        return getAccount(userManager.getCallerPrincipalName());
    }

}
