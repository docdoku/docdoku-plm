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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.ListOfValues;
import com.docdoku.core.meta.ListOfValuesKey;
import com.docdoku.core.meta.NameValuePair;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ILOVManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.*;

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
 * @author Lebeau Julien on 03/03/15.
 */
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(ILOVManagerLocal.class)
@Stateless(name = "LOVManagerBean")
public class LOVManagerBean implements ILOVManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<ListOfValues> findLOVFromWorkspace(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {

        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        LOVDAO lovDAO = new LOVDAO(new Locale(user.getLanguage()), em);
        return lovDAO.loadLOVList(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ListOfValues findLov(ListOfValuesKey lovKey) throws ListOfValuesNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(lovKey.getWorkspaceId());
        LOVDAO lovDAO = new LOVDAO(new Locale(user.getLanguage()), em);

        return lovDAO.loadLOV(lovKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void createLov(String workspaceId, String name, List<NameValuePair> nameValuePairList) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, ListOfValuesAlreadyExistsException, CreationException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        LOVDAO lovDAO = new LOVDAO(locale, em);

        if (name == null || name.trim().isEmpty()) {
            throw new CreationException("LOVNameEmptyException");
        }

        if (nameValuePairList == null || nameValuePairList.isEmpty()) {
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
    public void deleteLov(ListOfValuesKey lovKey) throws ListOfValuesNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, EntityConstraintException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(lovKey.getWorkspaceId());
        userManager.checkWorkspaceWriteAccess(lovKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        LOVDAO lovDAO = new LOVDAO(locale, em);

        if (isLovUsedInDocumentMasterTemplate(lovKey)) {
            throw new EntityConstraintException(locale, "EntityConstraintException14");
        }

        if (isLovUsedInPartMasterTemplate(lovKey)) {
            throw new EntityConstraintException(locale, "EntityConstraintException15");
        }

        ListOfValues lov = lovDAO.loadLOV(lovKey);
        lovDAO.deleteLOV(lov);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public ListOfValues updateLov(ListOfValuesKey lovKey, String name, String workspaceId, List<NameValuePair> nameValuePairList) throws ListOfValuesAlreadyExistsException, CreationException, ListOfValuesNotFoundException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, WorkspaceNotEnabledException {
        User user = userManager.checkWorkspaceReadAccess(lovKey.getWorkspaceId());
        userManager.checkWorkspaceWriteAccess(lovKey.getWorkspaceId());
        LOVDAO lovDAO = new LOVDAO(new Locale(user.getLanguage()), em);

        ListOfValues lovToUpdate = findLov(lovKey);

        lovToUpdate.setValues(nameValuePairList);

        return lovDAO.updateLOV(lovToUpdate);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public boolean isLOVDeletable(ListOfValuesKey lovKey) {
        return !isLovUsedInDocumentMasterTemplate(lovKey) && !isLovUsedInPartMasterTemplate(lovKey) && !isLovUsedInPartIterationInstanceAttributeTemplates(lovKey);
    }

    private boolean isLovUsedInDocumentMasterTemplate(ListOfValuesKey lovKey) {
        DocumentMasterTemplateDAO documentMasterTemplateDAO = new DocumentMasterTemplateDAO(em);
        List<DocumentMasterTemplate> documentsUsingLOV = documentMasterTemplateDAO.findAllDocMTemplatesFromLOV(lovKey);
        return documentsUsingLOV != null && !documentsUsingLOV.isEmpty();
    }

    private boolean isLovUsedInPartMasterTemplate(ListOfValuesKey lovKey) {
        PartMasterTemplateDAO partMasterTemplateDAO = new PartMasterTemplateDAO(em);
        List<PartMasterTemplate> partsUsingLOV = partMasterTemplateDAO.findAllPartMTemplatesFromLOV(lovKey);
        return partsUsingLOV != null && !partsUsingLOV.isEmpty();
    }

    private boolean isLovUsedInPartIterationInstanceAttributeTemplates(ListOfValuesKey lovKey) {
        PartIterationDAO partIterationDAO = new PartIterationDAO(em);
        List<PartIteration> partsUsingLOV = partIterationDAO.findAllPartIterationFromLOV(lovKey);
        return partsUsingLOV != null && !partsUsingLOV.isEmpty();
    }

}
