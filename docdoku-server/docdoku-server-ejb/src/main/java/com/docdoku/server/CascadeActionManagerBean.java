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

import com.docdoku.core.configuration.CascadeResult;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.ConfigurationItemKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.ICascadeActionManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Charles Fallourd on 10/02/16.
 */
@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID})
@Local(ICascadeActionManagerLocal.class)
@Stateless(name = "CascadeActionManagerBean")
public class CascadeActionManagerBean implements ICascadeActionManagerLocal {

    private static final Logger LOGGER = Logger.getLogger(ProductManagerBean.class.getName());

    @EJB
    private IProductManagerLocal productManager;

    //Every action should be transactional
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public CascadeResult cascadeCheckOut(ConfigurationItemKey configurationItemKey, String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        CascadeResult cascadeResult = new CascadeResult();

        Set<PartRevision> partRevisions = productManager.getWritablePartRevisionsFromPath(configurationItemKey,path);

        for(PartRevision pr : partRevisions) {
            try {
                productManager.checkOutPart(pr.getKey());
                cascadeResult.incSucceedAttempts();
            } catch (PartRevisionNotFoundException | AccessRightException  | NotAllowedException | FileAlreadyExistsException | CreationException e) {
                cascadeResult.incFailedAttempts();
                LOGGER.log(Level.SEVERE,null,e);
            }
        }
        return cascadeResult;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public CascadeResult cascadeUndoCheckOut(ConfigurationItemKey configurationItemKey, String path) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, NotAllowedException, EntityConstraintException, PartMasterNotFoundException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {
        CascadeResult cascadeResult = new CascadeResult();
        Set<PartRevision> partRevisions = productManager.getWritablePartRevisionsFromPath(configurationItemKey, path);
        for(PartRevision pr : partRevisions) {
            try {
                productManager.undoCheckOutPart(pr.getKey());
                cascadeResult.incSucceedAttempts();
            } catch (PartRevisionNotFoundException | AccessRightException  | NotAllowedException  e) {
                cascadeResult.incFailedAttempts();
                LOGGER.log(Level.SEVERE,null,e);
            }
        }
        return cascadeResult;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public CascadeResult cascadeCheckIn(ConfigurationItemKey configurationItemKey, String path, String iterationNote) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, PartMasterNotFoundException, EntityConstraintException, NotAllowedException, PartUsageLinkNotFoundException, ConfigurationItemNotFoundException, WorkspaceNotEnabledException {

        CascadeResult cascadeResult = new CascadeResult();
        Set<PartRevision> partRevisions = productManager.getWritablePartRevisionsFromPath(configurationItemKey, path);
        for(PartRevision pr : partRevisions) {
            try {
                // Set the iteration note only if param is set and part has no iteration note
                if( (iterationNote != null && iterationNote.isEmpty()) && (null == pr.getLastIteration().getIterationNote() && pr.getLastIteration().getIterationNote().isEmpty())){
                    productManager.updatePartIteration(pr.getLastIteration().getKey(), iterationNote, null, null, null, null, null, null, null);
                }

                productManager.checkInPart(pr.getKey());
                cascadeResult.incSucceedAttempts();

            } catch (DocumentRevisionNotFoundException | PartRevisionNotFoundException | AccessRightException  | NotAllowedException | ESServerException | ListOfValuesNotFoundException e) {
                cascadeResult.incFailedAttempts();
                LOGGER.log(Level.SEVERE,null,e);
            }
        }
        return cascadeResult;
    }
}
