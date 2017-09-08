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
package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.hooks.SNSWebhookApp;
import com.docdoku.core.hooks.SimpleWebhookApp;
import com.docdoku.core.hooks.Webhook;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWebhookManagerLocal;
import com.docdoku.server.dao.WebhookDAO;
import com.docdoku.server.dao.WorkspaceDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;

/**
 * @author Morgan Guimard
 */
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID})
@Local(IWebhookManagerLocal.class)
@Stateless(name = "WebhookManagerBean")
public class WebhookManagerBean implements IWebhookManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IContextManagerLocal contextManager;

    @Inject
    private IUserManagerLocal userManager;


    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Webhook createWebhook(String workspaceId, String name, boolean active)
            throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException, AccessRightException, AccountNotFoundException {
        Account account = userManager.checkAdmin(workspaceId);
        WorkspaceDAO workspaceDAO = new WorkspaceDAO(new Locale(account.getLanguage()), em);
        Workspace workspace = workspaceDAO.loadWorkspace(workspaceId);
        Webhook webhook = new Webhook(new SimpleWebhookApp(), name, active, workspace);
        new WebhookDAO(new Locale(account.getLanguage()), em).createWebhook(webhook);
        return webhook;
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public List<Webhook> getWebHooks(String workspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException {
        Account account = userManager.checkAdmin(workspaceId);
        WebhookDAO webhookDAO = new WebhookDAO(new Locale(account.getLanguage()), em);
        return webhookDAO.loadWebhooks(workspaceId);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Webhook getWebHook(String workspaceId, int id) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException {
        Account account = userManager.checkAdmin(workspaceId);
        Locale locale = new Locale(account.getLanguage());
        Webhook webhook = new WebhookDAO(locale, em).loadWebhook(id);
        if (!webhook.getWorkspace().getId().equals(workspaceId)) {
            throw new WebhookNotFoundException(locale, id);
        }
        return webhook;
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public Webhook updateWebHook(String workspaceId, int id, String name, boolean active) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException {
        Webhook webHook = getWebHook(workspaceId, id);
        webHook.setActive(active);
        webHook.setName(name);
        return webHook;
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void deleteWebhook(String workspaceId, int id) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException {
        Account account = userManager.checkAdmin(workspaceId);
        Locale locale = new Locale(account.getLanguage());
        WebhookDAO webhookDAO = new WebhookDAO(locale, em);
        Webhook webhook = webhookDAO.loadWebhook(id);
        if (!webhook.getWorkspace().getId().equals(workspaceId)) {
            throw new WebhookNotFoundException(locale, id);
        }
        webhookDAO.removeWebook(webhook);
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public SimpleWebhookApp configureSimpleWebhook(String workspaceId, Integer webhookId, String method, String uri, String authorization) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException {
        Webhook webHook = getWebHook(workspaceId, webhookId);
        SimpleWebhookApp app = new SimpleWebhookApp(method, authorization, uri);
        webHook.setWebhookApp(app);
        return app;
    }

    @Override
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public SNSWebhookApp configureSNSWebhook(String workspaceId, Integer webhookId, String topicArn, String region, String awsAccount, String awsSecret) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException {
        Webhook webHook = getWebHook(workspaceId, webhookId);
        SNSWebhookApp app = new SNSWebhookApp(topicArn, region, awsAccount, awsSecret);
        webHook.setWebhookApp(app);
        return app;
    }

}
