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
import com.docdoku.core.hooks.Webhook;

import java.util.List;

/**
 * @author Morgan Guimard
 */
public interface IWebhookManagerLocal {
    Webhook createWebhook(String workspaceId, String name, boolean active) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, WorkspaceNotEnabledException, AccessRightException, AccountNotFoundException;

    List<Webhook> getWebHooks(String workspaceId) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException;
    Webhook getWebHook(String workspaceId, int id) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException;

    void enableWebhook(String workspaceId, int id, boolean enable) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException;

    void deleteWebhook(String workspaceId, int id) throws WorkspaceNotFoundException, AccountNotFoundException, AccessRightException, WebhookNotFoundException;
}
