/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentLink;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.Folder;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.services.*;
import com.docdoku.core.workflow.WorkflowModel;

import javax.annotation.Resource;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class WorkspaceDAO {

    @Resource(name = "vaultPath")
    private String vaultPath;

    private EntityManager em;
    private Locale mLocale;
    private IDataManagerLocal dataManager;

    public WorkspaceDAO(Locale pLocale, EntityManager pEM) {
        em=pEM;
        mLocale=pLocale;
    }
    
    public WorkspaceDAO(EntityManager pEM) {
        em=pEM;
        mLocale=Locale.getDefault();
    }

    public WorkspaceDAO(EntityManager pEM, IDataManagerLocal pDataManager) {
        em=pEM;
        dataManager = pDataManager;
    }

    public void updateWorkspace(Workspace pWorkspace){
        em.merge(pWorkspace);
    }
    
    public void createWorkspace(Workspace pWorkspace) throws WorkspaceAlreadyExistsException, CreationException, FolderAlreadyExistsException {
        try{
            //the EntityExistsException is thrown only when flush occurs
            em.persist(pWorkspace);
            em.flush();
            new FolderDAO(mLocale, em).createFolder(new Folder(pWorkspace.getId()));
        }catch(EntityExistsException pEEEx){
            throw new WorkspaceAlreadyExistsException(mLocale, pWorkspace);
        }catch(PersistenceException pPEx){
            //EntityExistsException is case sensitive
            //whereas MySQL is not thus PersistenceException could be
            //thrown instead of EntityExistsException
            throw new CreationException(mLocale);
        }
    }
    
    
    public Workspace loadWorkspace(String pID) throws WorkspaceNotFoundException {
        Workspace workspace=em.find(Workspace.class,pID);      
        if (workspace == null) {
            throw new WorkspaceNotFoundException(mLocale, pID);
        } else {
            return workspace;
        }
    }

    public Long getDiskUsageForWorkspace(String pWorkspaceId) {
        Number result = ((Number)em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/%")
                .getSingleResult());

        return result != null ? result.longValue() : 0L;
    }

    public List<Workspace> findWorkspacesWhereUserIsActive(String userLogin){
        List<Workspace> workspaces = em.createNamedQuery("Workspace.findWorkspacesWhereUserIsActive")
                .setParameter("userLogin", userLogin)
                .getResultList();
        return workspaces;
    }

    public void removeWorkspace(Workspace workspace) throws IOException, StorageException {

        String workspaceId = workspace.getId();
        String pathToMatch = workspaceId+"/%";

        // Keep binaries in memory to delete them if google storage is the default storage provider
        // We also could enhance the way we delete files by using gsutils from google api
        List<BinaryResource> binaryResourcesInWorkspace =
            em.createQuery("SELECT b FROM BinaryResource b where b.fullName LIKE :pathToMatch ")
                .setParameter("pathToMatch",pathToMatch).getResultList();

        // SharedEntities
        em.createQuery("DELETE FROM SharedEntity s where s.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Subscriptions
        em.createQuery("DELETE FROM IterationChangeSubscription s where s.observedDocumentMaster.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        em.createQuery("DELETE FROM StateChangeSubscription s where s.observedDocumentMaster.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Baselines
        em.createQuery("DELETE FROM Baseline b where b.configurationItem.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // EffectivityConfigSpecs
        em.createQuery("DELETE FROM EffectivityConfigSpec e where e.configurationItem.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Effectivity
        em.createQuery("DELETE FROM Effectivity e where e.configurationItem.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Layers
        em.createQuery("DELETE FROM Layer l where l.configurationItem.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();
        // Markers
        em.createQuery("DELETE FROM Marker m where m.author.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // ConfigurationItem
        em.createQuery("DELETE FROM ConfigurationItem c where c.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // DocumentMasterTemplate
        em.createQuery("DELETE FROM DocumentMasterTemplate d where d.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // PartMasterTemplate
        em.createQuery("DELETE FROM PartMasterTemplate p where p.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        em.flush();

        // Clear all document links ...
        List<DocumentIteration> documentsIteration =
                em.createQuery("SELECT d FROM DocumentIteration d WHERE d.documentMaster.workspace = :workspace")
                        .setParameter("workspace",workspace).getResultList();

        List<PartIteration> partsIteration =
                em.createQuery("SELECT p FROM PartIteration p WHERE p.partRevision.partMaster.workspace = :workspace")
                        .setParameter("workspace",workspace).getResultList();

        for (DocumentIteration d: documentsIteration) {
            d.setLinkedDocuments(new HashSet<DocumentLink>());
        }
        for (PartIteration p: partsIteration) {
            p.setLinkedDocuments(new HashSet<DocumentLink>());
        }
        em.flush();


        // Remove parents

        List<DocumentMaster> documentsMaster =
                em.createQuery("SELECT d FROM DocumentMaster d WHERE d.workspace = :workspace")
                        .setParameter("workspace",workspace).getResultList();

        for (DocumentMaster d: documentsMaster) {
            em.remove(d);
        }
        em.flush();


        List<PartMaster> partsMaster =
                em.createQuery("SELECT p FROM PartMaster p WHERE p.workspace = :workspace")
                        .setParameter("workspace",workspace).getResultList();

        for (PartMaster p: partsMaster) {
            em.remove(p);
        }
        em.flush();

        // Delete folders
        em.createQuery("DELETE FROM Folder f where f.parentFolder.completePath = :workspaceId OR f.parentFolder.completePath LIKE :pathToMatch ")
                .setParameter("workspaceId",workspaceId).setParameter("pathToMatch",pathToMatch).executeUpdate();
        em.createQuery("DELETE FROM Folder f where f.completePath = :workspaceId OR f.completePath LIKE :pathToMatch ")
                .setParameter("workspaceId",workspaceId).setParameter("pathToMatch",pathToMatch).executeUpdate();


        List<WorkflowModel> workflowModels =
                em.createQuery("SELECT w FROM WorkflowModel w WHERE w.workspace = :workspace")
                        .setParameter("workspace",workspace).getResultList();

        for (WorkflowModel w: workflowModels) {
            em.remove(w);
        }
        em.flush();


        // Tags
        em.createQuery("DELETE FROM Tag t where t.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();
        // Roles
        em.createQuery("DELETE FROM Role r where r.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();


        // WorkspaceUserGroupMembership
        em.createQuery("DELETE FROM WorkspaceUserGroupMembership w where w.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // WorkspaceUserMembership
        em.createQuery("DELETE FROM WorkspaceUserMembership w where w.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // User groups
        em.createQuery("DELETE FROM UserGroup u where u.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        List<UserGroup> userGroups =
                em.createQuery("SELECT u FROM UserGroup u WHERE u.workspace = :workspace")
                        .setParameter("workspace",workspace).getResultList();

        for (UserGroup u: userGroups) {
            u.setUsers(new HashSet<User>());
            em.flush();
            em.remove(u);
        }

        // Users
        em.createQuery("DELETE FROM User u where u.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Finally delete the workspace

        em.flush();

        em.remove(workspace);

        //em.createQuery("DELETE FROM Workspace w where w.id = :workspaceId")
        //        .setParameter("workspaceId",workspaceId).executeUpdate();

        // Delete workspace files
        dataManager.deleteWorkspaceFolder(workspaceId,binaryResourcesInWorkspace);

    }
}