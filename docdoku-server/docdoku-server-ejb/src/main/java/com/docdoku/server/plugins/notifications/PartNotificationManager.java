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


import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.events.CheckedIn;
import com.docdoku.server.events.PartIterationChangeEvent;
import com.docdoku.server.events.PartRevisionChangeEvent;
import com.docdoku.server.events.Removed;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Named;

/**
 * @author Florent Garin
 */
@Named
@RequestScoped
public class PartNotificationManager {


    @EJB
    private IProductManagerLocal productService;


    private void onRemovePartIteration(@Observes @Removed PartIterationChangeEvent event){
        PartIteration partIteration = event.getModifiedPart();
        productService.removeModificationNotificationsOnIteration(partIteration.getKey());
    }

    private void onRemovePartRevision(@Observes @Removed PartRevisionChangeEvent event){
        PartRevision partRevision = event.getModifiedPart();
        productService.removeModificationNotificationsOnRevision(partRevision.getKey());
    }
    private void onCheckInPartIteration(@Observes @CheckedIn PartIterationChangeEvent event) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartRevisionNotFoundException, AccessRightException {
        PartIteration partIteration = event.getModifiedPart();
        productService.createModificationNotifications(partIteration);
    }
}
