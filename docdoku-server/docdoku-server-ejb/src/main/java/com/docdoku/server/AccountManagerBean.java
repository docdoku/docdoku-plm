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

import com.docdoku.core.admin.PlatformOptions;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IAccountManagerLocal;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IPlatformOptionsManagerLocal;
import com.docdoku.server.dao.AccountDAO;
import com.docdoku.server.dao.GCMAccountDAO;
import com.docdoku.server.dao.OrganizationDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@DeclareRoles({UserGroupMapping.GUEST_PROXY_ROLE_ID, UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(IAccountManagerLocal.class)
@Stateless(name = "AccountManagerBean")
public class AccountManagerBean implements IAccountManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IMailerLocal mailer;

    @Inject
    private IPlatformOptionsManagerLocal platformOptionsManager;

    private static final Logger LOGGER = Logger.getLogger(AccountManagerBean.class.getName());

    public AccountManagerBean() {
    }

    @Override
    public Account authenticateAccount(String login, String password) {
        AccountDAO accountDAO = new AccountDAO(em);
        Account account = null;

        if(accountDAO.authenticate(login, password)){
            try {
                account = getAccount(login);
            } catch (AccountNotFoundException e) {
               return null;
            }
        }

        return account;
    }

    @Override
    public Account createAccount(String pLogin, String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountAlreadyExistsException, CreationException {
        PlatformOptions.OperationSecurityStrategy registrationStrategy = platformOptionsManager.getRegistrationStrategy();
        Date now = new Date();
        Account account = new Account(pLogin, pName, pEmail, pLanguage, now, pTimeZone);
        account.setEnabled(registrationStrategy.equals(PlatformOptions.OperationSecurityStrategy.NONE));
        new AccountDAO(new Locale(pLanguage), em).createAccount(account, pPassword);
        mailer.sendCredential(account);
        return account;
    }

    @Override
    public Account getAccount(String pLogin) throws AccountNotFoundException {
        return new AccountDAO(em).loadAccount(pLogin);
    }

    public String getRole(String login) {
        UserGroupMapping userGroupMapping = em.find(UserGroupMapping.class, login);
        if (userGroupMapping == null) {
            return null;
        } else {
            return userGroupMapping.getGroupName();
        }
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account updateAccount(String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountNotFoundException {
        AccountDAO accountDAO = new AccountDAO(new Locale(pLanguage), em);
        Account account = accountDAO.loadAccount(contextManager.getCallerPrincipalLogin());
        account.setName(pName);
        account.setEmail(pEmail);
        account.setLanguage(pLanguage);
        account.setTimeZone(pTimeZone);
        if (pPassword != null) {
            accountDAO.updateCredential(account.getLogin(), pPassword);
        }
        return account;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(Organization pOrganization) throws AccessRightException, AccountNotFoundException {
        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());

        if (!contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) && !pOrganization.getOwner().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }

        return account;
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account checkAdmin(String pOrganizationName)
            throws AccessRightException, AccountNotFoundException, OrganizationNotFoundException {

        Account account = new AccountDAO(em).loadAccount(contextManager.getCallerPrincipalLogin());
        Organization organization = new OrganizationDAO(em).loadOrganization(pOrganizationName);

        if (!contextManager.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID) && !organization.getOwner().equals(account)) {
            throw new AccessRightException(new Locale(account.getLanguage()), account);
        }

        return account;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void setGCMAccount(String gcmId) throws AccountNotFoundException, GCMAccountAlreadyExistsException, CreationException {
        String callerLogin = contextManager.getCallerPrincipalLogin();
        Account account = getAccount(callerLogin);
        GCMAccountDAO gcmAccountDAO = new GCMAccountDAO(em);

        try {
            GCMAccount gcmAccount = gcmAccountDAO.loadGCMAccount(account);
            gcmAccount.setGcmId(gcmId);
        } catch (GCMAccountNotFoundException e) {
            gcmAccountDAO.createGCMAccount(new GCMAccount(account, gcmId));
        }

    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public void deleteGCMAccount() throws AccountNotFoundException, GCMAccountNotFoundException {
        String callerLogin = contextManager.getCallerPrincipalLogin();
        Account account = getAccount(callerLogin);
        GCMAccountDAO gcmAccountDAO = new GCMAccountDAO(new Locale(account.getLanguage()), em);
        GCMAccount gcmAccount = gcmAccountDAO.loadGCMAccount(account);
        gcmAccountDAO.deleteGCMAccount(gcmAccount);
    }

    @Override
    public boolean isAccountEnabled(String pLogin) throws AccountNotFoundException {
        Account account = getAccount(pLogin);
        return account.isEnabled();
    }

    @Override
    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    public List<Account> getAccounts() {
        AccountDAO accountDAO = new AccountDAO(em);
        return accountDAO.getAccounts();
    }

    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
    @Override
    public Account getMyAccount() throws AccountNotFoundException {
        return getAccount(contextManager.getCallerPrincipalName());
    }

    @Override
    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    public Account enableAccount(String login, boolean enabled) throws AccountNotFoundException, NotAllowedException {
        String callerPrincipalLogin = contextManager.getCallerPrincipalLogin();
        if (!callerPrincipalLogin.equals(login)) {
            Account account = getAccount(login);
            account.setEnabled(enabled);
            return account;
        } else {
            Account callerAccount = getMyAccount();
            throw new NotAllowedException(new Locale(callerAccount.getLanguage()), "NotAllowedException67");
        }
    }

    @Override
    @RolesAllowed(UserGroupMapping.ADMIN_ROLE_ID)
    public Account updateAccount(String pLogin, String pName, String pEmail, String pLanguage, String pPassword, String pTimeZone) throws AccountNotFoundException, NotAllowedException {
        Account account = getMyAccount();
        AccountDAO accountDAO = new AccountDAO(new Locale(account.getLanguage()), em);
        Account otherAccount = getAccount(pLogin);
        otherAccount.setName(pName);
        otherAccount.setEmail(pEmail);
        otherAccount.setLanguage(pLanguage);
        otherAccount.setTimeZone(pTimeZone);
        if (pPassword != null) {
            accountDAO.updateCredential(otherAccount.getLogin(), pPassword);
        }
        return otherAccount;
    }
}
