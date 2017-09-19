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
package com.docdoku.core.services;

import com.docdoku.core.exceptions.*;
import com.docdoku.core.hooks.SNSWebhookApp;
import com.docdoku.core.hooks.SimpleWebhookApp;
import com.docdoku.core.hooks.Webhook;

import java.util.List;

/**
 * @author Morgan Guimard
 */
public interface IWebhookManagerLocal {
    Webhook createWebhook(String workspaceId, String name, boolean active) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException, AccessRightException, AccountNotFoundException;

    List<Webhook> getWebHooks(String workspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException;

    Webhook getWebHook(String workspaceId, int id) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException;

    Webhook updateWebHook(String workspaceId, int id, String name, boolean active) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException;

    void deleteWebhook(String workspaceId, int id) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException;

    SimpleWebhookApp configureSimpleWebhook(String workspaceId, int webhookId, String method, String uri, String authorization) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException;

    SNSWebhookApp configureSNSWebhook(String workspaceId, int webhookId, String topicArn, String region, String awsAccount, String awsSecret) throws WorkspaceNotFoundException, AccessRightException, WebhookNotFoundException, AccountNotFoundException;

    List<Webhook> getActiveWebHooks(String workspaceId) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException;
}
