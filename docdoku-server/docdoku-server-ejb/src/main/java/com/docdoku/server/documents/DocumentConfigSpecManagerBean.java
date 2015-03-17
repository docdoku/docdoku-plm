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
import com.docdoku.core.configuration.BaselinedFolder;
import com.docdoku.core.configuration.BaselinedFolderKey;
import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.meta.Tag;
import com.docdoku.core.meta.TagKey;
import com.docdoku.core.query.DocumentSearchQuery;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDocumentBaselineManagerLocal;
import com.docdoku.core.services.IDocumentConfigSpecManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.configuration.spec.BaselineDocumentConfigSpec;
import com.docdoku.server.configuration.spec.LatestDocumentConfigSpec;
import com.docdoku.server.dao.BaselinedDocumentDAO;
import com.docdoku.server.dao.BaselinedFolderDAO;
import com.docdoku.server.dao.FolderDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@DeclareRoles({UserGroupMapping.REGULAR_USER_ROLE_ID, UserGroupMapping.ADMIN_ROLE_ID, UserGroupMapping.GUEST_PROXY_ROLE_ID})
@Local(IDocumentConfigSpecManagerLocal.class)
@Stateless(name = "DocumentConfigSpecManagerBean")
public class DocumentConfigSpecManagerBean implements IDocumentConfigSpecManagerLocal {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private IUserManagerLocal userManager;
    @EJB
    private IDocumentBaselineManagerLocal documentBaselineService;
    @EJB
    private IDocumentManagerLocal documentService;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentConfigSpec getLatestConfigSpec(String workspaceId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        return new LatestDocumentConfigSpec(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentConfigSpec getConfigSpecForBaseline(int baselineId) throws BaselineNotFoundException, WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException {
        DocumentBaseline documentBaseline = documentBaselineService.getBaseline(baselineId);
        User user = userManager.checkWorkspaceReadAccess(documentBaseline.getWorkspace().getId());
        return new BaselineDocumentConfigSpec(documentBaseline,user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public String[] getFilteredFolders(String workspaceId, DocumentConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        String[] shortNames;
        if(cs!=null&& cs instanceof BaselineDocumentConfigSpec){
            int collectionId = ((BaselineDocumentConfigSpec) cs).getDocumentBaseline().getFolderCollection().getId();
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

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getFilteredDocuments(String workspaceId, DocumentConfigSpec cs, int start, int pMaxResults) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = Arrays.asList(documentService.getAllDocumentsInWorkspace(workspaceId, start, pMaxResults));
        List<DocumentRevision> documentRevisionList = new ArrayList<>(docRs);
        return filterDocumentRevisionList(cs, documentRevisionList).toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getFilteredDocumentsByFolder(String workspaceId, DocumentConfigSpec cs, String completePath) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException {
        User user = userManager.checkWorkspaceReadAccess(workspaceId);
        if(cs instanceof BaselineDocumentConfigSpec){
            return getFilteredDocumentsByFolder((BaselineDocumentConfigSpec) cs,completePath,user);
        }else{
            return documentService.findDocumentRevisionsByFolder(completePath);
        }
    }

    private DocumentRevision[] getFilteredDocumentsByFolder(BaselineDocumentConfigSpec cs, String completePath, User user) {
        BaselinedFolderKey key = new BaselinedFolderKey(cs.getFolderCollectionId(),completePath);
        List<DocumentIteration> baselinedDocuments = new BaselinedDocumentDAO(new Locale(user.getLanguage()),em).findDocRsByFolder(key);
        List<DocumentRevision> returnList = new ArrayList<>();
        for(DocumentIteration baselinedDocument : baselinedDocuments){
            DocumentRevision docR = filterDocumentRevisionAccessRight(user,baselinedDocument.getDocumentRevision());
            returnList.add(docR);
        }
        return returnList.toArray(new DocumentRevision[returnList.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] getFilteredDocumentsByTag(String workspaceId, DocumentConfigSpec cs, TagKey tagKey) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = Arrays.asList(documentService.findDocumentRevisionsByTag(tagKey));
        List<DocumentRevision> documentRevisionList = new ArrayList<>(docRs);
        return filterDocumentRevisionList(cs, documentRevisionList).toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision[] searchFilteredDocuments(String workspaceId, DocumentConfigSpec cs, DocumentSearchQuery pQuery) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, DocumentRevisionNotFoundException, ESServerException {
        userManager.checkWorkspaceReadAccess(workspaceId);
        List<DocumentRevision> docRs = Arrays.asList(documentService.searchDocumentRevisions(pQuery));
        List<DocumentRevision> documentRevisionList = new ArrayList<>(docRs);
        return filterDocumentRevisionList(cs, documentRevisionList).toArray(new DocumentRevision[docRs.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public DocumentRevision getFilteredDocumentRevision(DocumentRevisionKey documentRevisionKey, DocumentConfigSpec configSpec) throws AccessRightException, NotAllowedException, WorkspaceNotFoundException, UserNotFoundException, DocumentRevisionNotFoundException, UserNotActiveException {
        DocumentRevision docR = documentService.getDocumentRevision(documentRevisionKey);
        docR = filterDocumentRevision(configSpec,docR);
        return docR;
    }

    private List<DocumentRevision> filterDocumentRevisionList(DocumentConfigSpec configSpec, List<DocumentRevision> pDocumentRs) throws DocumentRevisionNotFoundException {
        List<DocumentRevision> returnList = new ArrayList<>();
        for(DocumentRevision documentRevision : pDocumentRs){
            returnList.add(filterDocumentRevision(configSpec, documentRevision));
        }
        return returnList;
    }

    private DocumentRevision filterDocumentRevision(DocumentConfigSpec configSpec, DocumentRevision documentRevision) throws DocumentRevisionNotFoundException {
        Locale locale = Locale.getDefault();
        if(documentRevision==null){
            throw new DocumentRevisionNotFoundException("");
        }

        DocumentIteration docI = configSpec.filter(documentRevision);
        if(docI!=null){
            em.detach(documentRevision);

            if(documentRevision.getNumberOfIterations() > 1){
                documentRevision.removeFollowingIterations(docI.getIteration());
            }
            documentRevision.setCheckOutDate(null);
            documentRevision.setCheckOutUser(null);
            documentRevision.setWorkflow(null);
            documentRevision.setTags(new HashSet<Tag>());
            return documentRevision;
        }

        throw new DocumentRevisionNotFoundException(locale,documentRevision.getKey());
    }

    private DocumentRevision filterDocumentRevisionAccessRight(User user, DocumentRevision documentRevision){
        if(!user.isAdministrator()
                && (documentRevision.getACL()!=null)
                && !(documentRevision.getACL().hasReadAccess(user))) {
            return null;
        }
        return documentRevision;
    }
}