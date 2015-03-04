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

import com.docdoku.core.meta.ListOfValues;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ILOVManagerLocal;
import com.docdoku.server.dao.LOVDAO;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by lebeaujulien on 03/03/15.
 */
@Local(ILOVManagerLocal.class)
@Stateless(name = "LOVManagerBean")
public class LOVManagerBean implements ILOVManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ListOfValues> findLOVFromWorkspace(String workspaceId) {

        LOVDAO lovDAO = new LOVDAO(em);
        return lovDAO.loadLOVList(workspaceId);
    }
}
