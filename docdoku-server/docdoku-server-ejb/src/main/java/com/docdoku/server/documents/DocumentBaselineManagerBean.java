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
package com.docdoku.server.documents;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.BaselinedDocument;
import com.docdoku.core.configuration.BaselinedDocumentKey;
import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.configuration.DocumentCollection;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.DocumentBaselineDAO;
import com.docdoku.server.dao.DocumentRevisionDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IDocumentBaselineManagerLocal.class)
@Stateless(name = "DocumentBaselineManagerBean")
public class DocumentBaselineManagerBean implements IDocumentBaselineManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private IDocumentManagerLocal documentService;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentBaseline createBaseline(String workspaceId, String name, DocumentBaseline.BaselineType type, String description, List<DocumentRevisionKey> documentRevisionKeys)
            throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FolderNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException, NotAllowedException {

        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        if (documentRevisionKeys.isEmpty()) {
            throw new NotAllowedException(new Locale(user.getLanguage()), "NotAllowedException66");
        }
        DocumentBaseline baseline = new DocumentBaseline(user, name, type, description);
        baseline.getDocumentCollection().setCreationDate(new Date());
        baseline.getDocumentCollection().setAuthor(user);
        new DocumentBaselineDAO(em, new Locale(user.getLanguage())).createBaseline(baseline);
        snapshotDocuments(baseline, workspaceId, documentRevisionKeys);
        return baseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public List<DocumentBaseline> getBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        DocumentBaselineDAO documentBaselineDAO = new DocumentBaselineDAO(em, new Locale(user.getLanguage()));
        return documentBaselineDAO.findBaselines(workspaceId);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteBaseline(int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException, UserNotActiveException {
        DocumentBaseline documentBaseline = getBaselineLight(baselineId);
        User user = userManager.checkWorkspaceWriteAccess(documentBaseline.getAuthor().getWorkspaceId());
        new DocumentBaselineDAO(em, new Locale(user.getLanguage())).deleteBaseline(documentBaseline);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentBaseline getBaselineLight(int baselineId) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        DocumentBaseline documentBaseline = new DocumentBaselineDAO(em).loadBaseline(baselineId);
        userManager.checkWorkspaceReadAccess(documentBaseline.getAuthor().getWorkspaceId());
        return documentBaseline;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentCollection getACLFilteredDocumentCollection(int baselineId) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        DocumentBaseline documentBaseline = new DocumentBaselineDAO(em).loadBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(documentBaseline.getAuthor().getWorkspaceId());
        DocumentCollection filteredDocumentCollection = new DocumentCollection();

        for (Map.Entry<BaselinedDocumentKey, BaselinedDocument> map : documentBaseline.getDocumentCollection().getBaselinedDocuments().entrySet()) {
            DocumentIteration documentIteration = map.getValue().getTargetDocument();
            DocumentRevision documentRevision = filterDocumentRevisionAccessRight(user, documentIteration.getDocumentRevision());
            if (documentRevision != null) {
                filteredDocumentCollection.addBaselinedDocument(documentIteration);
            }
        }

        return filteredDocumentCollection;
    }

    private DocumentRevision filterDocumentRevisionAccessRight(User user, DocumentRevision documentRevision){
        if(!user.isAdministrator()
                && (documentRevision.getACL()!=null)
                && !(documentRevision.getACL().hasReadAccess(user))) {
            return null;
        }
        return documentRevision;
    }

    private void snapshotDocuments(DocumentBaseline baseline, String workspaceId, List<DocumentRevisionKey> documentRevisionKeys) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, FolderNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);

        for (DocumentRevisionKey revisionKey : documentRevisionKeys) {
            // Ignore already existing document
            if (baseline.hasBaselinedDocument(revisionKey)) {
                continue;
            }

            DocumentIteration documentIteration = filterBaselinedDocument(baseline.getType(), revisionKey);

            if (documentIteration != null) {
                baseline.addBaselinedDocument(documentIteration);
            }
        }
    }

    private DocumentIteration filterBaselinedDocument(DocumentBaseline.BaselineType type, DocumentRevisionKey documentRevisionKey) throws DocumentRevisionNotFoundException {
        DocumentRevisionDAO documentRevisionDAO = new DocumentRevisionDAO(em);
        DocumentRevision documentRevision = documentRevisionDAO.loadDocR(documentRevisionKey);

        if (type.equals(DocumentBaseline.BaselineType.RELEASED)) {
            return documentRevision.isReleased() ? documentRevision.getLastIteration() : null;

        } else {
            if (documentRevision.isCheckedOut()) {
                em.detach(documentRevision);
                documentRevision.removeLastIteration();
            }
            return documentRevision.getLastIteration();
        }
    }
}