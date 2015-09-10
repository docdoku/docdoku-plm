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
package com.docdoku.server.plugins.notifications;


import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.events.Removed;
import com.docdoku.server.events.UserRemovedEvent;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Morgan Guimard
 */
@Named
@RequestScoped
public class UserNotificationManager {

    @Inject
    private IDocumentManagerLocal documentService;

    private void onRemoveUser(@Observes @Removed UserRemovedEvent userRemovedEvent) throws ESServerException, EntityConstraintException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, DocumentRevisionNotFoundException, FolderNotFoundException, AccessRightException, UserNotActiveException {
        User user = userRemovedEvent.getRemovedUser();
        documentService.deleteUserFolder(user);
    }

}
