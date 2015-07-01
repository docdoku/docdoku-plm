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
package com.docdoku.server.rest;

import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.server.rest.dto.ModificationNotificationDTO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Florent Garin
 */

@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class ModificationNotificationResource {

    @EJB
    private IProductManagerLocal productService;

    public ModificationNotificationResource() {
    }

    @PUT
    @Path("/{notificationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response acknowledgeNotification(@PathParam("workspaceId") String workspaceId, @PathParam("notificationId") int notificationId, ModificationNotificationDTO notificationDTO)
            throws UserNotFoundException, AccessRightException, PartRevisionNotFoundException, WorkspaceNotFoundException {

        productService.updateModificationNotification(workspaceId, notificationId, notificationDTO.getAckComment());
        return Response.ok().build();
    }


}