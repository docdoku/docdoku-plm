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

import com.docdoku.core.meta.ListOfValues;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ILOVManagerLocal;
import com.docdoku.server.rest.dto.ListOfValuesDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

import javax.annotation.PostConstruct;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lebeaujulien on 03/03/15.
 */
@Stateless
@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
public class LOVResource {

    @EJB
    private ILOVManagerLocal lovManager;

    private Mapper mapper;

    public LOVResource(){}

    @PostConstruct
    public void init() {
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ListOfValuesDTO> getLovs(@PathParam("workspaceId") String workspaceId){
        List<ListOfValuesDTO> lovsDTO = new ArrayList<>();
        List<ListOfValues> lovs = lovManager.findLOVFromWorkspace(workspaceId);

        for (ListOfValues lov : lovs){
            lovsDTO.add(mapper.map(lov, ListOfValuesDTO.class));
        }

        return lovsDTO;
    }
}
