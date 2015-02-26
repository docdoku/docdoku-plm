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
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.BaselinedFolder;
import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.configuration.FolderCollection;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.DocumentBaselineDAO;
import com.docdoku.server.dao.DocumentRevisionDAO;
import com.docdoku.server.dao.FolderDAO;
import com.docdoku.server.dao.WorkspaceDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IDocumentBaselineManagerLocal.class)
@Stateless(name = "DocumentBaselineManagerBean")
public class DocumentBaselineManagerBean implements IDocumentBaselineManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IDocumentManagerLocal documentService;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentBaseline createBaseline(String workspaceId, String name, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FolderNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Workspace workspace = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        DocumentBaseline baseline = new DocumentBaseline(workspace,name,description);
        new DocumentBaselineDAO(em, new Locale(user.getLanguage())).createBaseline(baseline);
        snapshotAllFolders(baseline,workspaceId);
        snapshotAllDocuments(baseline,workspaceId);
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
        DocumentBaseline documentBaseline = getBaseline(baselineId);
        User user = userManager.checkWorkspaceWriteAccess(documentBaseline.getWorkspace().getId());
        new DocumentBaselineDAO(em, new Locale(user.getLanguage())).deleteBaseline(documentBaseline);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentBaseline getBaseline(int baselineId) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        DocumentBaseline documentBaseline = new DocumentBaselineDAO(em).loadBaseline(baselineId);
        userManager.checkWorkspaceReadAccess(documentBaseline.getWorkspace().getId());
        return documentBaseline;
    }

    private void fillBaselineFolder(DocumentBaseline baseline, String folderPath) throws FolderNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException{
        // Ignore already existing folder
        if(baseline.hasBasedLinedFolder(folderPath)){
            return;
        }
        // Add current folder
        FolderDAO folderDAO = new FolderDAO(em);

        Folder currentFolder = folderDAO.loadFolder(folderPath);
        BaselinedFolder baselinedFolder = new BaselinedFolder(baseline.getFolderCollection(),currentFolder);
        baseline.addBaselinedFolder(baselinedFolder);

        // Add all subFolders
        Folder[] subFolders = folderDAO.getSubFolders(folderPath);
        for(Folder subFolder : subFolders){
            fillBaselineFolder(baseline, subFolder.getCompletePath());
        }
    }

    private void fillBaselineDocument(DocumentBaseline baseline, List<DocumentRevisionKey> revisionKeys) throws DocumentRevisionNotFoundException, FolderNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        // Add all document
        for(DocumentRevisionKey revisionKey : revisionKeys){
            User user = userManager.checkWorkspaceReadAccess(revisionKey.getDocumentMaster().getWorkspace());

            // Ignore already existing document
            if(baseline.hasBaselinedDocument(revisionKey)){
                break;
            }

            DocumentRevision documentRevision = new DocumentRevisionDAO(em).loadDocR(revisionKey);
            documentRevision = filterDocumentRevisionBaselinable(user, documentRevision);
            // Document non accessible
            if(documentRevision==null){
                break;
            }

            DocumentIteration documentIteration = documentRevision.getLastIteration();
            if(documentIteration!=null){
                baseline.addBaselinedDocument(documentIteration);
            }
        }
    }

    private void snapshotAllFolders(DocumentBaseline baseline, String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FolderNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        FolderCollection collection = baseline.getFolderCollection();
        collection.setCreationDate(new Date());
        collection.setAuthor(user);
        fillBaselineFolder(baseline, workspaceId);
    }

    private void snapshotAllDocuments(DocumentBaseline baseline, String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, FolderNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        DocumentRevision[] documentRevisions = documentService.getAllDocumentsInWorkspace(workspaceId);
        List<DocumentRevisionKey> revisionKeyList = new ArrayList<>();
        for(DocumentRevision documentRevision : documentRevisions){
            revisionKeyList.add(documentRevision.getKey());
        }

        fillBaselineDocument(baseline, revisionKeyList);
    }

    private DocumentRevision filterDocumentRevisionAccessRight(User user, DocumentRevision documentRevision){
        if(!user.isAdministrator()
                && (documentRevision.getACL()!=null)
                && !(documentRevision.getACL().hasReadAccess(user))) {
            return null;
        }
        return documentRevision;
    }

    private DocumentRevision filterDocumentRevisionBaselinable(User user, DocumentRevision documentRevision){
        DocumentRevision documentFiltered =filterDocumentRevisionAccessRight(user,documentRevision);

        if (documentFiltered!= null && documentFiltered.isCheckedOut()) {
            em.detach(documentFiltered);
            documentFiltered.removeLastIteration();
        }
        return documentFiltered;
    }
}