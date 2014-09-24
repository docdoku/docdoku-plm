/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
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
import com.docdoku.core.configuration.BaselineConfigSpec;
import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.configuration.LatestConfigSpec;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.document.baseline.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.dao.*;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@DeclareRoles({"users","admin","guest-proxy"})
@Local(IDocumentBaselineManagerLocal.class)
@Stateless(name = "DocumentBaselineManagerBean")
public class DocumentBaselineManagerBean implements IDocumentBaselineManagerLocal{

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IDocumentManagerLocal documentService;

    @RolesAllowed("users")
    @Override
    public DocumentBaseline createBaseline(String workspaceId, String name, String description) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, FolderNotFoundException, UserNotActiveException, DocumentRevisionNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Workspace workspace = new WorkspaceDAO(new Locale(user.getLanguage()), em).loadWorkspace(workspaceId);
        DocumentBaseline baseline = new DocumentBaseline(workspace,name,description);
        new DocumentBaselineDAO(em).createBaseline(baseline);
        snapshotAllFolders(baseline,workspaceId);
        snapshotAllDocuments(baseline,workspaceId);
        return baseline;
    }

    @RolesAllowed("users")
    @Override
    public List<DocumentBaseline> getBaselines(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        DocumentBaselineDAO documentBaselineDAO = new DocumentBaselineDAO(em, new Locale(user.getLanguage()));
        return documentBaselineDAO.findBaselines(workspaceId);
    }

    @RolesAllowed("users")
    @Override
    public void deleteBaseline(int baselineId) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, BaselineNotFoundException, UserNotActiveException {
        DocumentBaseline documentBaseline = getBaseline(baselineId);
        userManager.checkWorkspaceWriteAccess(documentBaseline.getWorkspace().getId());
        new DocumentBaselineDAO(em).deleteBaseline(documentBaseline);
    }

    @RolesAllowed("users")
    @Override
    public DocumentBaseline getBaseline(int baselineId) throws BaselineNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        DocumentBaseline documentBaseline = new DocumentBaselineDAO(em).loadBaseline(baselineId);
        userManager.checkWorkspaceReadAccess(documentBaseline.getWorkspace().getId());
        return documentBaseline;
    }

    @RolesAllowed("users")
    @Override
    public ConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestConfigSpec(user);
    }

    @RolesAllowed("users")
    @Override
    public ConfigSpec getConfigSpecForBaseline(int baselineId) throws BaselineNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException {
        DocumentBaseline documentBaseline = getBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(documentBaseline.getWorkspace().getId());
        return new BaselineConfigSpec(documentBaseline,user);
    }

    @RolesAllowed("users")
    @Override
    public String[] getFilteredFolders(String workspaceId, ConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        String[] shortNames;
        if(cs!=null&& cs instanceof BaselineConfigSpec){
            int collectionId = ((BaselineConfigSpec) cs).getDocumentBaseline().getFoldersCollection().getId();
            BaselinedFolderKey key = new BaselinedFolderKey(collectionId,completePath);
            List<BaselinedFolder> subFolders = new BaselinedFolderDAO(locale, em).getSubFolders(key);
            shortNames = new String[subFolders.size()];
            int i = 0;
            for (BaselinedFolder f : subFolders) {
                shortNames[i++] = f.getShortName();
            }
        }else{
            Folder[] subFolders = new FolderDAO(locale, em).getSubFolders(completePath);
            shortNames = new String[subFolders.length];
            int i = 0;
            for (Folder f : subFolders) {
                shortNames[i++] = f.getShortName();
            }
        }
        return shortNames;
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getFilteredDocuments(String workspaceId, ConfigSpec cs, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = Arrays.asList(documentService.getAllDocumentsInWorkspace(workspaceId, start, pMaxResults));
        List<DocumentRevision> documentRevisionList = new ArrayList<>(docRs);
        return filterDocumentRevisionList(cs, documentRevisionList).toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getFilteredDocumentsByFolder(String workspaceId, ConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        BaselinedFolderKey key = new BaselinedFolderKey(cs.getId(),completePath);
        List<BaselinedDocument> baselinedDocuments = new BaselinedDocumentDAO(new Locale(user.getLanguage()),em).findDocRsByFolder(key);
        List<DocumentRevision> returnList = new ArrayList<>();
        for(BaselinedDocument baselinedDocument : baselinedDocuments){
            DocumentIteration docI = baselinedDocument.getTargetDocument();
            DocumentRevision docR = filterDocumentRevisionAccessRight(user,docI.getDocumentRevision());
            returnList.add(docR);
        }
        return returnList.toArray(new DocumentRevision[returnList.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] getFilteredDocumentsByTag(String workspaceId, ConfigSpec cs, TagKey tagKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = Arrays.asList(documentService.findDocumentRevisionsByTag(tagKey));
        List<DocumentRevision> documentRevisionList = new ArrayList<>(docRs);
        return filterDocumentRevisionList(cs, documentRevisionList).toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision[] searchFilteredDocuments(String workspaceId, ConfigSpec cs, DocumentSearchQuery pQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, ESServerException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = Arrays.asList(documentService.searchDocumentRevisions(pQuery));
        List<DocumentRevision> documentRevisionList = new ArrayList<>(docRs);
        return filterDocumentRevisionList(cs, documentRevisionList).toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed("users")
    @Override
    public DocumentRevision getFilteredDocumentRevision(DocumentRevisionKey documentRevisionKey, ConfigSpec configSpec) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException {
        DocumentRevision docR = documentService.getDocumentRevision(documentRevisionKey);
        docR = filterDocumentRevision(configSpec,docR);
        return docR;
    }

    private void fillBaselineFolder(DocumentBaseline baseline, String folderPath) throws FolderNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException{
        // Ignore already existing folder
        if(baseline.hasBasedLinedFolder(folderPath)){
            return;
        }
        // Add current folder
        FolderDAO folderDAO = new FolderDAO(em);

        Folder currentFolder = folderDAO.loadFolder(folderPath);
        BaselinedFolder baselinedFolder = new BaselinedFolder(baseline.getFoldersCollection(),currentFolder);
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
            User user = userManager.checkWorkspaceReadAccess(revisionKey.getWorkspaceId());

            // Ignore already existing document
            if(baseline.hasBasedLinedDocument(revisionKey.getDocumentMaster())){
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
                // Add current
                BaselinedDocument baselinedDocument = baseline.addBaselinedDocument(documentIteration);
                int collectionId = baseline.getFoldersCollection().getId();
                BaselinedFolderKey key = new BaselinedFolderKey(collectionId,
                        documentIteration.getDocumentRevision().getLocation().getCompletePath());
                BaselinedFolder baselinedFolder = new BaselinedFolderDAO(em).loadBaselineFolder(key);
                baselinedDocument.setBaselinedFolder(baselinedFolder);
            }
        }
    }

    private void snapshotAllFolders(DocumentBaseline baseline, String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, FolderNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        FoldersCollection collection = baseline.getFoldersCollection();
        collection.setCreationDate(new Date());
        collection.setAuthor(user);
        fillBaselineFolder(baseline, workspaceId);
    }

    private void snapshotAllDocuments(DocumentBaseline baseline, String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, FolderNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        DocumentsCollection collection = baseline.getDocumentsCollection();
        collection.setCreationDate(new Date());
        collection.setAuthor(user);
        DocumentRevision[] documentRevisions = documentService.getAllDocumentsInWorkspace(workspaceId);
        List<DocumentRevisionKey> revisionKeyList = new ArrayList<>();
        for(DocumentRevision documentRevision : documentRevisions){
            revisionKeyList.add(documentRevision.getKey());
        }

        fillBaselineDocument(baseline, revisionKeyList);
    }

    private List<DocumentRevision> filterDocumentRevisionList(ConfigSpec configSpec, List<DocumentRevision> pDocumentRs) throws DocumentRevisionNotFoundException {
        List<DocumentRevision> returnList = new ArrayList<>();
        for(DocumentRevision documentRevision : pDocumentRs){
            returnList.add(filterDocumentRevision(configSpec, documentRevision));
        }
        return returnList;
    }

    private DocumentRevision filterDocumentRevision(ConfigSpec configSpec, DocumentRevision documentRevision) throws DocumentRevisionNotFoundException {
        DocumentIteration docI = configSpec.filterConfigSpec(documentRevision.getDocumentMaster());
        if(docI!=null){
            DocumentRevision docR = docI.getDocumentRevision();
            em.detach(docR);
            if(docR!=null && docR.getNumberOfIterations() > 1){
                docR.removeLastIterations(docI.getIteration());
                docR.setWorkflow(null);
                docR.setTags(new HashSet<Tag>());
                return docR;
            }
        }
        throw new DocumentRevisionNotFoundException("");
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

        if (documentFiltered.isCheckedOut()) {
            em.detach(documentFiltered);
            documentFiltered.removeLastIteration();
        }
        return documentFiltered;
    }
}