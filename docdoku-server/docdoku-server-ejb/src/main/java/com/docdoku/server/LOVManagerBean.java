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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.ListOfValues;
import com.docdoku.core.meta.ListOfValuesKey;
import com.docdoku.core.meta.NameValuePair;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ILOVManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.LOVDAO;
import com.docdoku.server.dao.WorkspaceDAO;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lebeau Julien on 03/03/15.
 */
@Local(ILOVManagerLocal.class)
@Stateless(name = "LOVManagerBean")
public class LOVManagerBean implements ILOVManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ListOfValues> findLOVFromWorkspace(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        LOVDAO lovDAO = new LOVDAO(new Locale(user.getLanguage()),em);
        return lovDAO.loadLOVList(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ListOfValues findLov(ListOfValuesKey lovKey) throws ListOfValuesNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(lovKey.getWorkspaceId());
        LOVDAO lovDAO = new LOVDAO(new Locale(user.getLanguage()),em);

        return lovDAO.loadLOV(lovKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void createLov(String workspaceId, String name, List<NameValuePair> nameValuePairList) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ListOfValuesAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        LOVDAO lovDAO = new LOVDAO(locale, em);

        if (name == null || name.trim().equalsIgnoreCase("")){
            throw new CreationException("LOVNameEmptyException");
        }

        if (nameValuePairList == null || nameValuePairList.size() == 0){
            throw new CreationException("LOVPossibleValueException");
        }

        WorkspaceDAO workspaceDAO = new WorkspaceDAO(locale, em);
        Workspace workspace = workspaceDAO.loadWorkspace(workspaceId);

        ListOfValues lov = new ListOfValues(workspace, name);
        lov.setValues(nameValuePairList);

        lovDAO.createLOV(lov);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteLov(ListOfValuesKey lovKey) throws ListOfValuesNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(lovKey.getWorkspaceId());
        userManager.checkWorkspaceWriteAccess(lovKey.getWorkspaceId());
        LOVDAO lovDAO = new LOVDAO(new Locale(user.getLanguage()),em);
        ListOfValues lov = lovDAO.loadLOV(lovKey);
        lovDAO.deleteLOV(lov);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ListOfValues updateLov(ListOfValuesKey lovKey, String name, String workspaceId, List<NameValuePair> nameValuePairList) throws ListOfValuesAlreadyExistsException, CreationException, ListOfValuesNotFoundException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException {
        User user = userManager.checkWorkspaceReadAccess(lovKey.getWorkspaceId());
        userManager.checkWorkspaceWriteAccess(lovKey.getWorkspaceId());
        this.deleteLov(lovKey);
        this.createLov(workspaceId, name, nameValuePairList);
        ListOfValuesKey newLovKey = new ListOfValuesKey(workspaceId, name);
        return this.findLov(newLovKey);
    }


}
