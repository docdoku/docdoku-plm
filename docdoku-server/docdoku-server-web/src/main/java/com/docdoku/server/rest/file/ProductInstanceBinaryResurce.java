package com.docdoku.server.rest.file; /*
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

import com.docdoku.core.exceptions.EntityNotFoundException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.security.UserGroupMapping;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/*
 *
 * @author Asmae CHADID on 30/03/15.
 */
public class ProductInstanceBinaryResurce {
    @POST
    @Path("{serialNumber}/iterations/{iteration}/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({UserGroupMapping.REGULAR_USER_ROLE_ID})
    public void uploadFilesToProductInstanceIteration(@PathParam("workspaceId") String workspaceId, @PathParam("ciId") String configurationItemId, @PathParam("serialNumber") String serialNumber, @PathParam("iteration") int iteration)
            throws EntityNotFoundException, UserNotActiveException {


        return ;
    }
}
