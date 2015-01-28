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
package com.docdoku.server.dao;

import com.docdoku.core.change.ChangeIssue;
import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.ChangeIssueNotFoundException;
import com.docdoku.core.exceptions.ChangeOrderNotFoundException;
import com.docdoku.core.exceptions.ChangeRequestNotFoundException;
import com.docdoku.core.meta.Tag;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChangeItemDAO {

    private EntityManager em;
    private Locale mLocale;
    private static final String WORKSPACEID = "workspaceId";

    public ChangeItemDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale = pLocale;
    }

    public ChangeItemDAO(EntityManager pEM) {
        em = pEM;
        mLocale = Locale.getDefault();
    }


    public List<ChangeIssue> findAllChangeIssues(String pWorkspaceId) {
        return em.createNamedQuery("ChangeIssue.findChangeIssuesByWorkspace", ChangeIssue.class)
                 .setParameter(WORKSPACEID, pWorkspaceId)
                 .getResultList();
    }
    public List<ChangeRequest> findAllChangeRequests(String pWorkspaceId) {
        return em.createNamedQuery("ChangeRequest.findChangeRequestsByWorkspace", ChangeRequest.class)
                 .setParameter(WORKSPACEID, pWorkspaceId)
                 .getResultList();
    }
    public List<ChangeOrder> findAllChangeOrders(String pWorkspaceId) {
        return em.createNamedQuery("ChangeOrder.findChangeOrdersByWorkspace", ChangeOrder.class)
                 .setParameter(WORKSPACEID, pWorkspaceId)
                 .getResultList();
    }
    
    public ChangeIssue loadChangeIssue(int pId) throws ChangeIssueNotFoundException {
        ChangeIssue change = em.find(ChangeIssue.class, pId);
        if (change == null) {
            throw new ChangeIssueNotFoundException(mLocale, pId);
        } else {
            return change;
        }
    }
    public ChangeOrder loadChangeOrder(int pId) throws ChangeOrderNotFoundException {
        ChangeOrder change = em.find(ChangeOrder.class, pId);
        if (change == null) {
            throw new ChangeOrderNotFoundException(mLocale, pId);
        } else {
            return change;
        }
    }
    public ChangeRequest loadChangeRequest(int pId) throws ChangeRequestNotFoundException {
        ChangeRequest change = em.find(ChangeRequest.class, pId);
        if (change == null) {
            throw new ChangeRequestNotFoundException(mLocale, pId);
        } else {
            return change;
        }
    }

    public void createChangeItem(ChangeItem pChange) {
        if(pChange.getACL()!=null){
            ACLDAO aclDAO = new ACLDAO(em);
            aclDAO.createACL(pChange.getACL());
        }

        em.persist(pChange);
        em.flush();
    }
    public void deleteChangeItem(ChangeItem pChange) {
        em.remove(pChange);
        em.flush();
    }
    public ChangeItem removeTag(ChangeItem pChange, String tagName){
        Tag tagToRemove = new Tag(pChange.getWorkspace(), tagName);
        pChange.getTags().remove(tagToRemove);
        return pChange;
    }

    public List<ChangeIssue> findAllChangeIssuesWithReferenceLike(String pWorkspaceId, String reference, int maxResults) {
        return em.createNamedQuery("ChangeIssue.findByReference",ChangeIssue.class)
                .setParameter(WORKSPACEID, pWorkspaceId).setParameter("name", "%" + reference + "%").setMaxResults(maxResults).getResultList();
    }

    public List<ChangeRequest> findAllChangeRequestsWithReferenceLike(String pWorkspaceId, String reference, int maxResults) {
        return em.createNamedQuery("ChangeRequest.findByReference",ChangeRequest.class)
                .setParameter(WORKSPACEID, pWorkspaceId).setParameter("name", "%" + reference + "%").setMaxResults(maxResults).getResultList();
    }

    public List<ChangeItem> findChangeItemByTag(String pWorkspaceId, Tag tag){
        List<ChangeItem> changeItems = new ArrayList<>();
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeIssue c WHERE :tag MEMBER OF c.tags AND c.workspace.id = :workspaceId ", ChangeIssue.class)
                .setParameter("tag", tag)
                .setParameter(WORKSPACEID, pWorkspaceId)
                .getResultList());
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeRequest c WHERE :tag MEMBER OF c.tags AND c.workspace.id = :workspaceId ", ChangeRequest.class)
                .setParameter("tag", tag)
                .setParameter(WORKSPACEID, pWorkspaceId)
                .getResultList());
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeOrder c WHERE :tag MEMBER OF c.tags AND c.workspace.id = :workspaceId ", ChangeOrder.class)
                .setParameter("tag", tag)
                .setParameter(WORKSPACEID, pWorkspaceId)
                .getResultList());
        return changeItems;
    }

    public List<ChangeItem> findChangeItemByDoc(DocumentRevisionKey documentRevisionKey){
        List<ChangeItem> changeItems = new ArrayList<>();
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeIssue c , DocumentIteration i WHERE i member of c.affectedDocuments AND i.documentRevision.documentMaster.workspace.id = :workspaceId AND i.documentRevision.version = :version AND i.documentRevision.documentMasterId = :documentMasterId", ChangeIssue.class)
                .setParameter("workspaceId", documentRevisionKey.getWorkspaceId())
                .setParameter("documentMasterId", documentRevisionKey.getDocumentMasterId())
                .setParameter("version", documentRevisionKey.getVersion())                
                .getResultList());
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeRequest c , DocumentIteration i WHERE i member of c.affectedDocuments AND i.documentRevision.documentMaster.workspace.id = :workspaceId AND i.documentRevision.version = :version AND i.documentRevision.documentMasterId = :documentMasterId", ChangeRequest.class)
                .setParameter("workspaceId", documentRevisionKey.getWorkspaceId())
                .setParameter("documentMasterId", documentRevisionKey.getDocumentMasterId())
                .setParameter("version", documentRevisionKey.getVersion())
                .getResultList());
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeOrder c , DocumentIteration i WHERE i member of c.affectedDocuments AND i.documentRevision.documentMaster.workspace.id = :workspaceId AND i.documentRevision.version = :version AND i.documentRevision.documentMasterId = :documentMasterId", ChangeOrder.class)
                .setParameter("workspaceId", documentRevisionKey.getWorkspaceId())
                .setParameter("documentMasterId", documentRevisionKey.getDocumentMasterId())
                .setParameter("version", documentRevisionKey.getVersion())
                .getResultList());
        return changeItems;
    }
    
    public boolean hasChangeItems(DocumentRevisionKey documentRevisionKey){
        return !findChangeItemByDoc(documentRevisionKey).isEmpty();
    }


    public List<ChangeItem> findChangeItemByFolder(Folder folder){
        List<ChangeItem> changeItems = new ArrayList<>();
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeIssue c , DocumentIteration i WHERE i member of c.affectedDocuments AND i.documentRevision.location = :folder", ChangeIssue.class)
                .setParameter("folder", folder)
                .getResultList());
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeRequest c , DocumentIteration i WHERE i member of c.affectedDocuments AND i.documentRevision.location = :folder", ChangeRequest.class)
                .setParameter("folder", folder)
                .getResultList());
        changeItems.addAll(em.createQuery("SELECT c FROM ChangeOrder c , DocumentIteration i WHERE i member of c.affectedDocuments AND i.documentRevision.location = :folder", ChangeOrder.class)
                .setParameter("folder", folder)
                .getResultList());
        return changeItems;
    }


    public boolean hasConstraintsInFolderHierarchy(Folder folder) {
        boolean hasConstraints = false;

        FolderDAO folderDAO = new FolderDAO(mLocale,em);

        for(Folder subFolder : folderDAO.getSubFolders(folder)){
            hasConstraints |= hasConstraintsInFolderHierarchy(subFolder);
        }

        return hasConstraints || hasChangeItems(folder);

    }

    public boolean hasChangeItems(Folder folder) {
        return !findChangeItemByFolder(folder).isEmpty();
    }


}
