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
import com.docdoku.core.product.PartSubstituteLink;
import com.docdoku.core.product.PartUsageLink;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.workflow.WorkflowModel;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.*;

public class WorkspaceDAO {


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

    public long getDiskUsageForWorkspace(String pWorkspaceId) {
        Number result = (Number) em.createNamedQuery("BinaryResource.diskUsageInPath")
                .setParameter("path", pWorkspaceId+"/%")
                .getSingleResult();

        return result != null ? result.longValue() : 0L;
    }

    public List<Workspace> findWorkspacesWhereUserIsActive(String userLogin){
        return em.createNamedQuery("Workspace.findWorkspacesWhereUserIsActive", Workspace.class)
                .setParameter("userLogin", userLogin)
                .getResultList();
    }

    public void removeWorkspace(Workspace workspace) throws IOException, StorageException {

        String workspaceId = workspace.getId();
        String pathToMatch = workspaceId.replace("_","\\_").replace("%","\\%")+"/%";

        // Keep binaries in memory to delete them if google storage is the default storage provider
        // We also could enhance the way we delete files by using gsutils from google api
        List<BinaryResource> binaryResourcesInWorkspace =
            em.createQuery("SELECT b FROM BinaryResource b where b.fullName LIKE :pathToMatch", BinaryResource.class)
                .setParameter("pathToMatch",pathToMatch).getResultList();

        // SharedEntities
        em.createQuery("DELETE FROM SharedEntity s where s.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Subscriptions
        em.createQuery("DELETE FROM IterationChangeSubscription s where s.observedDocumentRevisionWorkspaceId = :workspaceId")
                .setParameter("workspaceId",workspaceId).executeUpdate();

        em.createQuery("DELETE FROM StateChangeSubscription s where s.observedDocumentRevisionWorkspaceId = :workspaceId")
                .setParameter("workspaceId",workspaceId).executeUpdate();

        // BaselinedPart
        em.createQuery("DELETE FROM BaselinedPart bp where bp.targetPart.partRevision.partMasterWorkspaceId = :workspaceId")
                .setParameter("workspaceId", workspaceId).executeUpdate();

        // BaselinedDocument
        em.createQuery("DELETE FROM BaselinedDocument bd where bd.targetDocument.documentRevision.documentMasterWorkspaceId = :workspaceId")
                .setParameter("workspaceId", workspaceId).executeUpdate();

        // ProductInstances
        em.createQuery("DELETE FROM ProductInstanceIteration pii where pii.productInstanceMaster.instanceOf.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();
        em.createQuery("DELETE FROM ProductInstanceMaster pim where pim.instanceOf.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();
        // ProductBaselines
        em.createQuery("DELETE FROM ProductBaseline b where b.configurationItem.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();
        // DocumentBaselines
        em.createQuery("DELETE FROM DocumentBaseline b where b.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Effectivity
        em.createQuery("DELETE FROM Effectivity e where e.configurationItem.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // PartCollection
        em.createQuery("DELETE FROM PartCollection pc where pc.author.workspaceId = :workspaceId")
                .setParameter("workspaceId",workspaceId).executeUpdate();

        // DocumentCollection
        em.createQuery("DELETE FROM DocumentCollection dc where dc.author.workspaceId = :workspaceId")
                .setParameter("workspaceId",workspaceId).executeUpdate();

        // FoldereCollection
        em.createQuery("DELETE FROM FolderCollection fc where fc.author.workspaceId = :workspaceId")
                .setParameter("workspaceId",workspaceId).executeUpdate();

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

        // Conversions
        em.createQuery("DELETE FROM Conversion c where c.partIteration.partRevision.partMaster.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Notifications
        em.createQuery("DELETE FROM ModificationNotification m where m.impactedPart.partRevision.partMaster.workspace = :workspace or m.modifiedPart.partRevision.partMaster.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Change issues
        em.createQuery("DELETE FROM ChangeIssue c where c.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Change requests
        em.createQuery("DELETE FROM ChangeRequest c where c.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Change issues
        em.createQuery("DELETE FROM ChangeOrder c where c.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Change issues / requests
        em.createQuery("DELETE FROM Milestone m where m.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Clear all document links ...
        List<DocumentIteration> documentsIteration =
                em.createQuery("SELECT d FROM DocumentIteration d WHERE d.documentRevision.documentMaster.workspace = :workspace", DocumentIteration.class)
                        .setParameter("workspace",workspace).getResultList();

        List<PartIteration> partsIteration =
                em.createQuery("SELECT p FROM PartIteration p WHERE p.partRevision.partMaster.workspace = :workspace", PartIteration.class)
                        .setParameter("workspace",workspace).getResultList();

        for (DocumentIteration d: documentsIteration) {
            d.setLinkedDocuments(new HashSet<DocumentLink>());
        }
        for (PartIteration p: partsIteration) {
            p.setLinkedDocuments(new HashSet<DocumentLink>());
            for (PartUsageLink pul: p.getComponents()) {
                pul.setSubstitutes(new LinkedList<PartSubstituteLink>());
            }
            p.setComponents(new LinkedList<PartUsageLink>());
        }
        em.flush();

        // Clear all part substitute links
        em.createQuery("DELETE FROM PartSubstituteLink psl WHERE psl.substitute.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Clear all part usage links
        em.createQuery("DELETE FROM PartUsageLink pul WHERE pul.component.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

        // Remove parents
        List<DocumentMaster> documentsMaster =
                em.createQuery("SELECT d FROM DocumentMaster d WHERE d.workspace = :workspace", DocumentMaster.class)
                        .setParameter("workspace",workspace).getResultList();

        for (DocumentMaster d: documentsMaster) {
            em.remove(d);
        }
        em.flush();


        List<PartMaster> partsMaster =
                em.createQuery("SELECT p FROM PartMaster p WHERE p.workspace = :workspace", PartMaster.class)
                        .setParameter("workspace",workspace).getResultList();

        for (PartMaster p: partsMaster) {
            em.remove(p);
        }
        em.flush();

        // Delete folders
        em.createQuery("DELETE FROM Folder f where f.parentFolder.completePath = :workspaceId OR f.parentFolder.completePath LIKE :pathToMatch")
                .setParameter("workspaceId",workspaceId).setParameter("pathToMatch",pathToMatch).executeUpdate();
        em.createQuery("DELETE FROM Folder f where f.completePath = :workspaceId OR f.completePath LIKE :pathToMatch")
                .setParameter("workspaceId",workspaceId).setParameter("pathToMatch",pathToMatch).executeUpdate();


        List<WorkflowModel> workflowModels =
                em.createQuery("SELECT w FROM WorkflowModel w WHERE w.workspace = :workspace", WorkflowModel.class)
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

        // LOV
        em.createQuery("DELETE FROM ListOfValuesAttributeTemplate lovat where lovat.lov.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();
        em.createQuery("DELETE FROM ListOfValues lov where lov.workspace = :workspace")
                .setParameter("workspace",workspace).executeUpdate();

        // Query
        em.createQuery("DELETE FROM QueryContext qc where qc.workspaceId = :workspaceId")
                .setParameter("workspaceId", workspaceId).executeUpdate();
        em.createQuery("DELETE FROM Query q where q.author.workspace = :workspace")
                .setParameter("workspace", workspace).executeUpdate();

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
                em.createQuery("SELECT u FROM UserGroup u WHERE u.workspace = :workspace",UserGroup.class)
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

        // Delete workspace files
        dataManager.deleteWorkspaceFolder(workspaceId,binaryResourcesInWorkspace);

        em.flush();

    }

    public List<Workspace> getAll() {
        return em.createNamedQuery("Workspace.findAllWorkspaces")
                .getResultList();
    }
}