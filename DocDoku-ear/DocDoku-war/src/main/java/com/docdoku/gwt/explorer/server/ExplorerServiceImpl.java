/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.gwt.explorer.server;

import com.docdoku.core.document.SearchQuery;
import com.docdoku.core.document.MasterDocumentTemplate;
import com.docdoku.core.document.InstanceAttributeTemplate;
import com.docdoku.core.security.WorkspaceUserGroupMembership;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.document.MasterDocumentKey;
import com.docdoku.core.document.TagKey;
import com.docdoku.core.document.Folder;
import com.docdoku.core.document.Tag;
import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.meta.InstanceBooleanAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceURLAttribute;
import com.docdoku.core.meta.InstanceNumberAttribute;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.document.DocumentToDocumentLink;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.document.DocumentKey;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.Document;
import com.docdoku.core.document.MasterDocument;
import com.docdoku.core.common.BasicElementKey;
import com.docdoku.core.workflow.TaskKey;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.ParallelActivity;
import com.docdoku.core.workflow.TaskModel;
import com.docdoku.core.workflow.Activity;
import com.docdoku.core.workflow.ParallelActivityModel;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.SerialActivityModel;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.Workflow;
import com.docdoku.core.services.ICommandLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.gwt.explorer.shared.*;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

/**
 *
 * @author Florent GARIN
 */
public class ExplorerServiceImpl extends RemoteServiceServlet implements ExplorerService {

    @EJB
    private ICommandLocal commandService;

    @EJB
    private IUserManagerLocal userManager;

    private Mapper mapper;
    
    @Override
    public void init(){
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @Override
    public String[] getFolders(String completePath) throws ApplicationException {
        try {
            return commandService.getFolders(completePath);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public String[] getTags(String workspaceId) throws ApplicationException {
        try {
            return commandService.getTags(workspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public WorkflowModelDTO[] getWorkflowModels(String workspaceId) throws ApplicationException {
        try {
            WorkflowModel[] wks = commandService.getWorkflowModels(workspaceId);
            WorkflowModelDTO[] data = new WorkflowModelDTO[wks.length];

            for (int i = 0; i < wks.length; i++) {
                data[i] = createDTO(wks[i]);
            }

            return data;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentTemplateDTO[] getMDocTemplates(String workspaceId) throws ApplicationException {
        try {
            MasterDocumentTemplate[] templates = commandService.getMDocTemplates(workspaceId);
            MasterDocumentTemplateDTO[] data = new MasterDocumentTemplateDTO[templates.length];

            for (int i = 0; i < templates.length; i++) {
                data[i] = createDTO(templates[i]);
            }

            return data;
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] getCheckedOutMDocs(String workspaceId) throws ApplicationException {
        try {
            MasterDocument[] mdocs = commandService.getCheckedOutMDocs(workspaceId);
            return setupMDocNotifications(createDTO(mdocs), workspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] findMDocsByFolder(String completePath) throws ApplicationException {
        try {
            MasterDocument[] mdocs = commandService.findMDocsByFolder(completePath);
            return setupMDocNotifications(createDTO(mdocs), completePath.split("/")[0]);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] findMDocsByTag(String workspaceId, String label) throws ApplicationException {
        try {
            MasterDocument[] mdocs = commandService.findMDocsByTag(new TagKey(workspaceId, label));
            return setupMDocNotifications(createDTO(mdocs), workspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void createFolder(String pParentFolder, String pFolder) throws ApplicationException {
        try {
            commandService.createFolder(pParentFolder, pFolder);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] delFolder(String completePath) throws ApplicationException {
        try {
            MasterDocumentKey[] mdocKeys = commandService.delFolder(completePath);
            return createDTO(mdocKeys);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO updateDoc(String workspaceId, String id, String version, int iteration, String revisionNote, InstanceAttributeDTO[] attributes, DocumentDTO[] links) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.updateDoc(new DocumentKey(workspaceId, id, version, iteration), revisionNote, createObject(attributes), createObject(links));
            return createDTO(mdoc);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO checkIn(String workspaceId, String id, String version) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.checkIn(new MasterDocumentKey(workspaceId, id, version));
            return createDTO(mdoc);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO checkOut(String workspaceId, String id, String version) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.checkOut(new MasterDocumentKey(workspaceId, id, version));
            return createDTO(mdoc);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO undoCheckOut(String workspaceId, String id, String version) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.undoCheckOut(new MasterDocumentKey(workspaceId, id, version));
            return createDTO(mdoc);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void delMDoc(String workspaceId, String id, String version) throws ApplicationException {
        try {
            commandService.delMDoc(new MasterDocumentKey(workspaceId, id, version));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO moveMDoc(String parentFolder, String workspaceId, String id, String version) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.moveMDoc(parentFolder, new MasterDocumentKey(workspaceId, id, version));
            return createDTO(mdoc);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public UserDTO whoAmI(String pWorkspaceId) throws ApplicationException {
        try {
            User user = commandService.whoAmI(pWorkspaceId);
            return mapper.map(user, UserDTO.class);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public String generateId(String workspaceId, String mdocTemplateId) throws ApplicationException {
        try {
            return commandService.generateId(workspaceId, mdocTemplateId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] createVersion(String pWorkspaceId, String pID, String pVersion, String pTitle, String pDescription, String pWorkflowModelId, ACLDTO acl) throws ApplicationException {
        try {
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(pWorkspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(pWorkspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }
            MasterDocument mdocs[] = commandService.createVersion(new MasterDocumentKey(pWorkspaceId, pID, pVersion), pTitle, pDescription, pWorkflowModelId, userEntries, userGroupEntries);
            return setupMDocNotifications(createDTO(mdocs), pWorkspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO removeFilesFromDocument(String[] pFullNames) throws ApplicationException {
        try {
            MasterDocument mdoc = null;
            for (String fullName : pFullNames) {
                mdoc = commandService.removeFileFromDocument(fullName);
            }
            return mdoc == null ? null : createDTO(mdoc);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO removeFileFromDocument(String pFullName) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.removeFileFromDocument(pFullName);
            return createDTO(mdoc);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentTemplateDTO removeFileFromTemplate(String pFullName) throws ApplicationException {
        try {
            MasterDocumentTemplate template = commandService.removeFileFromTemplate(pFullName);
            return createDTO(template);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentTemplateDTO removeFilesFromTemplate(String[] pFullNames) throws ApplicationException {
        try {
            MasterDocumentTemplate template = null;
            for (String fullName : pFullNames) {
                template = commandService.removeFileFromTemplate(fullName);
            }
            return template == null ? null : createDTO(template);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO getMDoc(String workspaceId, String id, String version) throws ApplicationException {
        try {
            MasterDocument mdoc = commandService.getMDoc(new MasterDocumentKey(workspaceId, id, version));
            return createDTO(mdoc);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentTemplateDTO getMDocTemplate(String workspaceId, String id) throws ApplicationException {
        try {
            MasterDocumentTemplate template = commandService.getMDocTemplate(new BasicElementKey(workspaceId, id));
            return createDTO(template);

        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    public MasterDocumentDTO createMDoc(String pParentFolder, String pMDocID, String pTitle, String pDescription, String pMDocTemplateId, String pWorkflowModelId, ACLDTO acl) throws ApplicationException {
        try {
            ACLUserEntry[] userEntries = null;
            ACLUserGroupEntry[] userGroupEntries = null;
            if (acl != null) {
                String workspaceId = Folder.parseWorkspaceId(pParentFolder);
                userEntries = new ACLUserEntry[acl.getUserEntries().size()];
                userGroupEntries = new ACLUserGroupEntry[acl.getGroupEntries().size()];
                int i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getUserEntries().entrySet()) {
                    userEntries[i] = new ACLUserEntry();
                    userEntries[i].setPrincipal(new User(new Workspace(workspaceId), entry.getKey()));
                    userEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
                i = 0;
                for (Map.Entry<String, ACLDTO.Permission> entry : acl.getGroupEntries().entrySet()) {
                    userGroupEntries[i] = new ACLUserGroupEntry();
                    userGroupEntries[i].setPrincipal(new UserGroup(new Workspace(workspaceId), entry.getKey()));
                    userGroupEntries[i++].setPermission(ACL.Permission.valueOf(entry.getValue().name()));
                }
            }
            MasterDocument mdoc = commandService.createMDoc(pParentFolder, pMDocID, pTitle, pDescription, pMDocTemplateId, pWorkflowModelId, userEntries, userGroupEntries);
            return createDTO(mdoc);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentTemplateDTO createMDocTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates, boolean idGenerated) throws ApplicationException {
        try {
            MasterDocumentTemplate template = commandService.createMDocTemplate(workspaceId, id, documentType, mask, createObject(attributeTemplates), idGenerated);
            return createDTO(template);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void delMDocTemplate(String workspaceId, String id) throws ApplicationException {
        try {
            commandService.delMDocTemplate(new BasicElementKey(workspaceId, id));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentTemplateDTO updateMDocTemplate(String workspaceId, String id, String documentType, String mask, InstanceAttributeTemplateDTO[] attributeTemplates, boolean idGenerated) throws ApplicationException {
        try {
            MasterDocumentTemplate template = commandService.updateMDocTemplate(new BasicElementKey(workspaceId, id), documentType, mask, createObject(attributeTemplates), idGenerated);
            return createDTO(template);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void delWorkflowModel(String workspaceId, String id) throws ApplicationException {
        try {
            commandService.delWorkflowModel(new BasicElementKey(workspaceId, id));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public UserDTO[] getUsers(String pWorkspaceId) throws ApplicationException {
        try {
            return createDTO(commandService.getUsers(pWorkspaceId));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void delTag(String workspaceId, String label) throws ApplicationException {
        try {
            commandService.delTag(new TagKey(workspaceId, label));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void createTag(String workspaceId, String label) throws ApplicationException {
        try {
            commandService.createTag(workspaceId, label);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO saveTags(String workspaceId, String id, String version, String[] tags) throws ApplicationException {
        try {
            return createDTO(commandService.saveTags(new MasterDocumentKey(workspaceId, id, version), tags));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public WorkflowModelDTO createWorkflowModel(String workspaceId, String id, String finalLifeCycleState, ActivityModelDTO[] activityModels) throws ApplicationException {
        try {
            return createDTO(commandService.createWorkflowModel(workspaceId, id, finalLifeCycleState, createObject(activityModels)));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] getIterationChangeEventSubscriptions(String workspaceId) throws ApplicationException {
        try {
            return createDTO(commandService.getIterationChangeEventSubscriptions(workspaceId));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] getStateChangeEventSubscriptions(String workspaceId) throws ApplicationException {
        try {
            return createDTO(commandService.getStateChangeEventSubscriptions(workspaceId));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void subscribeToIterationChangeEvent(String workspaceId, String id, String version) throws ApplicationException {
        try {
            commandService.subscribeToIterationChangeEvent(new MasterDocumentKey(workspaceId, id, version));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void subscribeToStateChangeEvent(String workspaceId, String id, String version) throws ApplicationException {
        try {
            commandService.subscribeToStateChangeEvent(new MasterDocumentKey(workspaceId, id, version));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void unsubscribeToIterationChangeEvent(String workspaceId, String id, String version) throws ApplicationException {
        try {
            commandService.unsubscribeToIterationChangeEvent(new MasterDocumentKey(workspaceId, id, version));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public void unsubscribeToStateChangeEvent(String workspaceId, String id, String version) throws ApplicationException {
        try {
            commandService.unsubscribeToStateChangeEvent(new MasterDocumentKey(workspaceId, id, version));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO approve(String workspaceId, int workflowId, int activityStep, int num, String comment) throws ApplicationException {
        try {
            return createDTO(commandService.approve(workspaceId, new TaskKey(workflowId, activityStep, num), comment));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO reject(String workspaceId, int workflowId, int activityStep, int num, String comment) throws ApplicationException {
        try {
            return createDTO(commandService.reject(workspaceId, new TaskKey(workflowId, activityStep, num), comment));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public MasterDocumentDTO[] searchMDocs(String workspaceId, String mdocId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content) throws ApplicationException {
        try {
            SearchQuery query = new SearchQuery(workspaceId, mdocId, title, version, author, type, creationDateFrom, creationDateTo, createObject(attributes), tags, content);
            return setupMDocNotifications(createDTO(commandService.searchMDocs(query)), workspaceId);
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }


    @Override
    public MDocTemplateResponse getMDocTemplates(String workspaceId, int startOffset, int chunkSize) throws ApplicationException {
        MasterDocumentTemplateDTO response[] = getMDocTemplates(workspaceId);
        if (startOffset < response.length){

            MasterDocumentTemplateDTO chunk[] ;

            if (startOffset + chunkSize <= response.length){
                chunk = new MasterDocumentTemplateDTO[chunkSize] ;
                int j = 0 ;
                for (int i = startOffset; i < startOffset + chunkSize; i++) {
                     chunk[j]= response[i];
                     j++;
                }
            }else{
                chunk = new MasterDocumentTemplateDTO[response.length - startOffset] ;
                int j = 0 ;
                for (int i = startOffset ; i < response.length ; i++){
                    chunk[j] = response[i] ;
                    j++ ;
                }
            }

            MDocTemplateResponse result = new MDocTemplateResponse();
            result.setChunckOffset(startOffset);
            result.setTotalSize(response.length);
            result.setData(chunk);
            return result;

        }

        MDocTemplateResponse defaultResponse = new MDocTemplateResponse() ;
        MasterDocumentTemplateDTO defaultChunk[] = new MasterDocumentTemplateDTO[0];
        defaultResponse.setTotalSize(0);
        defaultResponse.setChunckOffset(0);
        defaultResponse.setData(defaultChunk);

        return  defaultResponse;
    }

    @Override
    public WorkflowResponse getWorkflowModels(String workspaceId, int startOffset, int chunkSize) throws ApplicationException {
        WorkflowModelDTO response[] = getWorkflowModels(workspaceId);
        if (startOffset < response.length){

            WorkflowModelDTO chunk[] ;

            if (startOffset + chunkSize <= response.length){
                chunk = new WorkflowModelDTO[chunkSize] ;
                int j = 0 ;
                for (int i = startOffset; i < startOffset + chunkSize; i++) {
                     chunk[j]= response[i];
                     j++;
                }
            }else{
                chunk = new WorkflowModelDTO[response.length - startOffset] ;
                int j = 0 ;
                for (int i = startOffset ; i < response.length ; i++){
                    chunk[j] = response[i] ;
                    j++ ;
                }
            }

            WorkflowResponse result = new WorkflowResponse();
            result.setChunckOffset(startOffset);
            result.setTotalSize(response.length);
            result.setData(chunk);
            return result;

        }

        WorkflowResponse defaultResponse = new WorkflowResponse();
        defaultResponse.setChunckOffset(0);
        defaultResponse.setTotalSize(0);
        WorkflowModelDTO defaultChunk[] = new WorkflowModelDTO[0];
        defaultResponse.setData(defaultChunk);

        return defaultResponse ;
    }

    @Override
    public UserDTO[] getWorkspaceUserMemberships(String workspaceId) throws ApplicationException {
        try {
            return createDTO(userManager.getWorkspaceUserMemberships(workspaceId));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }

    @Override
    public UserGroupDTO[] getWorkspaceUserGroupMemberships(String workspaceId) throws ApplicationException {
        try {
            return createDTO(userManager.getWorkspaceUserGroupMemberships(workspaceId));
        } catch (com.docdoku.core.services.ApplicationException ex) {
            throw new ApplicationException(ex.getMessage());
        }
    }


    private UserGroupDTO[] createDTO(WorkspaceUserGroupMembership[] groupMSs) {
        UserGroupDTO[] dtos = new UserGroupDTO[groupMSs.length];
        for (int i = 0; i < groupMSs.length; i++) {
            dtos[i] = createDTO(groupMSs[i]);
        }
        return dtos;
    }

    private UserDTO[] createDTO(WorkspaceUserMembership[] userMSs) {
        UserDTO[] dtos = new UserDTO[userMSs.length];
        for (int i = 0; i < userMSs.length; i++) {
            dtos[i] = createDTO(userMSs[i]);
        }
        return dtos;
    }

    private UserGroupDTO createDTO(WorkspaceUserGroupMembership groupMS) {
        UserGroupDTO dto = new UserGroupDTO();
        dto.setId(groupMS.getMemberId());
        dto.setWorkspaceId(groupMS.getWorkspaceId());
        dto.setMembership(groupMS.isReadOnly() ? WorkspaceMembership.READ_ONLY : WorkspaceMembership.FULL_ACCESS);

        return dto;
    }

    private UserDTO createDTO(WorkspaceUserMembership userMS) {
        UserDTO dto = new UserDTO();
        dto.setLogin(userMS.getMemberLogin());
        dto.setWorkspaceId(userMS.getWorkspaceId());
        dto.setName(userMS.getMember().getName());
        dto.setEmail(userMS.getMember().getEmail());
        dto.setMembership(userMS.isReadOnly() ? WorkspaceMembership.READ_ONLY : WorkspaceMembership.FULL_ACCESS);
        return dto;
    }

    private MasterDocumentTemplateDTO createDTO(MasterDocumentTemplate template) {
        MasterDocumentTemplateDTO dto = new MasterDocumentTemplateDTO();
        dto.setId(template.getId());
        dto.setWorkspaceId(template.getWorkspaceId());
        dto.setAuthor(mapper.map(template.getAuthor(), UserDTO.class));
        dto.setDocumentType(template.getDocumentType());
        dto.setMask(template.getMask());
        dto.setCreationDate(template.getCreationDate());
        dto.setIdGenerated(template.isIdGenerated());

        Set<InstanceAttributeTemplateDTO> attrs = new HashSet<InstanceAttributeTemplateDTO>();
        for (InstanceAttributeTemplate attr : template.getAttributeTemplates()) {
            attrs.add(createDTO(attr));
        }
        dto.setAttributeTemplates(attrs);

        Map<String, String> files = new HashMap<String, String>();
        for (BinaryResource file : template.getAttachedFiles()) {
            files.put(file.getName(), file.getFullName());
        }
        dto.setAttachedFiles(files);

        return dto;
    }

    private InstanceAttributeTemplateDTO createDTO(InstanceAttributeTemplate attr) {
        InstanceAttributeTemplateDTO dto = new InstanceAttributeTemplateDTO();
        dto.setName(attr.getName());
        dto.setAttributeType(InstanceAttributeTemplateDTO.AttributeType.valueOf(attr.getAttributeType().name()));
        return dto;
    }

    private WorkflowModelDTO createDTO(WorkflowModel wk) {
        WorkflowModelDTO dto = new WorkflowModelDTO();
        dto.setId(wk.getId());
        dto.setWorkspaceId(wk.getWorkspaceId());
        dto.setAuthor(mapper.map(wk.getAuthor(), UserDTO.class));
        dto.setCreationDate(wk.getCreationDate());

        dto.setFinalLifeCycleState(wk.getFinalLifeCycleState());
        for (ActivityModel activity : wk.getActivityModels()) {
            ActivityModelDTO activityDTO = createDTO(activity);
            dto.addActivity(activityDTO);
        }

        return dto;
    }

    private WorkflowDTO createDTO(Workflow wk) {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setFinalLifeCycleState(wk.getFinalLifeCycleState());
        List<ActivityDTO> activities = new ArrayList<ActivityDTO>();
        for (Activity activity : wk.getActivities()) {
            activities.add(createDTO(activity));
        }
        dto.setActivities(activities);
        dto.setId(wk.getId());
        return dto;
    }

    private ActivityDTO createDTO(Activity activity) {
        ActivityDTO dto = null;
        if (activity instanceof ParallelActivity) {
            dto = new ParallelActivityDTO();
            ((ParallelActivityDTO) dto).setTasksToComplete(((ParallelActivity) activity).getTasksToComplete());
        } else {
            dto = new SerialActivityDTO();
        }
        dto.setStopped(activity.isStopped());
        dto.setComplete(activity.isComplete());
        dto.setLifeCycleState((activity.getLifeCycleState()));
        List<TaskDTO> tasks = new ArrayList<TaskDTO>();
        for (Task task : activity.getTasks()) {
            tasks.add(createDTO(task));
        }
        dto.setTasks(tasks);
        return dto;
    }

    private TaskDTO createDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setClosureComment(task.getClosureComment());
        dto.setClosureDate(task.getClosureDate());
        dto.setTargetIteration(task.getTargetIteration());
        dto.setTitle(task.getTitle());

        TaskDTO.Status s = TaskDTO.Status.APPROVED;
        switch (task.getStatus()) {
            case APPROVED:
                s = TaskDTO.Status.APPROVED;
                break;
            case IN_PROGRESS:
                s = TaskDTO.Status.IN_PROGRESS;
                break;
            case NOT_STARTED:
                s = TaskDTO.Status.NOT_STARTED;
                break;
            case REJECTED:
                s = TaskDTO.Status.REJECTED;
                break;
        }
        dto.setStatus(s);
        dto.setInstructions(task.getInstructions());
        dto.setWorker(mapper.map(task.getWorker(), UserDTO.class));
        return dto;
    }

    private ActivityModelDTO createDTO(ActivityModel activity) {

        ActivityModelDTO dto = null;
        if (activity instanceof SerialActivityModel) {
            dto = new SerialActivityModelDTO();

        } else {
            ParallelActivityModelDTO tmp = new ParallelActivityModelDTO();
            ParallelActivityModel tmpModel = (ParallelActivityModel) activity;
            tmp.setTasksToComplete(tmpModel.getTasksToComplete());
            dto = tmp;
        }

        for (TaskModel task : activity.getTaskModels()) {
            TaskModelDTO taskDTO = createDTO(task);
            dto.addTaskModel(taskDTO);
        }
        dto.setLifeCycleState(activity.getLifeCycleState());
        return dto;
    }

    private TaskModelDTO createDTO(TaskModel task) {
        TaskModelDTO dto = new TaskModelDTO();
        dto.setTitle(task.getTitle());
        dto.setWorker(mapper.map(task.getWorker(), UserDTO.class));
        dto.setInstructions(task.getInstructions());
        return dto;
    }

    private UserDTO[] createDTO(User[] users) {
        UserDTO[] data = new UserDTO[users.length];
        for (int i = 0; i < users.length; i++) {
            data[i] = mapper.map(users[i], UserDTO.class);
        }
        return data;
    }

    private MasterDocumentDTO[] createDTO(MasterDocumentKey[] mdocPKs) {
        MasterDocumentDTO[] data = new MasterDocumentDTO[mdocPKs.length];

        for (int i = 0; i < mdocPKs.length; i++) {
            data[i] = createDTO(mdocPKs[i]);
        }

        return data;
    }

    private MasterDocumentDTO createDTO(MasterDocumentKey mdocPK) {
        MasterDocumentDTO dto = new MasterDocumentDTO();
        dto.setWorkspaceID(mdocPK.getWorkspaceId());
        dto.setID(mdocPK.getId());
        dto.setVersion(mdocPK.getVersion());

        return dto;
    }

    private MasterDocumentDTO[] createDTO(MasterDocument[] mdocs) {
        MasterDocumentDTO[] data = new MasterDocumentDTO[mdocs.length];

        for (int i = 0; i < mdocs.length; i++) {
            data[i] = createDTO(mdocs[i]);
        }

        return data;
    }

    private MasterDocumentDTO createDTO(MasterDocument mdoc) {
        MasterDocumentDTO dto = new MasterDocumentDTO();
        dto.setWorkspaceID(mdoc.getWorkspaceId());
        dto.setID(mdoc.getId());
        dto.setVersion(mdoc.getVersion());
        dto.setAuthor(mapper.map(mdoc.getAuthor(), UserDTO.class));
        dto.setCheckOutDate(mdoc.getCheckOutDate());
        dto.setCheckOutUser((mdoc.getCheckOutUser() == null) ? null : mapper.map(mdoc.getCheckOutUser(), UserDTO.class));
        dto.setCreationDate(mdoc.getCreationDate());
        dto.setDescription(mdoc.getDescription());
        
        String[] tags = new String[mdoc.getTags().size()];
        int i = 0;
        for (Tag tag : mdoc.getTags()) {
            tags[i++] = tag.getLabel();
        }

        dto.setTags(tags);
        dto.setTitle(mdoc.getTitle());
        dto.setType(mdoc.getType());

        List<DocumentDTO> iterations = new ArrayList<DocumentDTO>();
        for (Document doc : mdoc.getDocumentIterations()) {
            iterations.add(createDTO(doc));
        }
        dto.setDocumentIterations(iterations);
        if (mdoc.getWorkflow() != null) {
            WorkflowDTO wk = createDTO(mdoc.getWorkflow());
            wk.setWorkspaceId(mdoc.getWorkspaceId());
            dto.setWorkflow(wk);
        }
        return dto;
    }

    private DocumentDTO createDTO(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setAuthor(mapper.map(doc.getAuthor(), UserDTO.class));
        dto.setCreationDate(doc.getCreationDate());
        dto.setIteration(doc.getIteration());
        dto.setMasterDocumentId(doc.getMasterDocumentId());
        dto.setMasterDocumentVersion(doc.getMasterDocumentVersion());
        dto.setWorkspaceId(doc.getWorkspaceId());
        dto.setRevisionNote(doc.getRevisionNote());

        Map<String, String> files = new HashMap<String, String>();
        for (BinaryResource file : doc.getAttachedFiles()) {
            files.put(file.getName(), file.getFullName());
        }
        dto.setAttachedFiles(files);

        Map<String, InstanceAttributeDTO> attributes = new HashMap<String, InstanceAttributeDTO>();
        for (Map.Entry<String, InstanceAttribute> entry : doc.getInstanceAttributes().entrySet()) {
            attributes.put(entry.getKey(), createDTO(entry.getValue()));
        }
        dto.setInstanceAttributes(attributes);

        Set<DocumentDTO> links = new HashSet<DocumentDTO>();
        for (DocumentToDocumentLink link : doc.getLinkedDocuments()) {
            links.add(new DocumentDTO(link.getToDocumentWorkspaceId(), link.getToDocumentMasterDocumentId(), link.getToDocumentMasterDocumentVersion(), link.getToDocumentIteration()));
        }
        dto.setLinkedDocuments(links);
        return dto;
    }

    private InstanceAttributeDTO createDTO(InstanceAttribute attr) {
        if (attr instanceof InstanceBooleanAttribute) {
            InstanceBooleanAttributeDTO dto = new InstanceBooleanAttributeDTO();
            dto.setName(attr.getName());
            dto.setBooleanValue((Boolean) attr.getValue());
            return dto;
        } else if (attr instanceof InstanceTextAttribute) {
            InstanceTextAttributeDTO dto = new InstanceTextAttributeDTO();
            dto.setName(attr.getName());
            dto.setTextValue((String) attr.getValue());
            return dto;
        } else if (attr instanceof InstanceNumberAttribute) {
            InstanceNumberAttributeDTO dto = new InstanceNumberAttributeDTO();
            dto.setName(attr.getName());
            dto.setNumberValue((Float) attr.getValue());
            return dto;
        } else if (attr instanceof InstanceDateAttribute) {
            InstanceDateAttributeDTO dto = new InstanceDateAttributeDTO();
            dto.setName(attr.getName());
            dto.setDateValue((Date) attr.getValue());
            return dto;
        } else if (attr instanceof InstanceURLAttribute) {
            InstanceURLAttributeDTO dto = new InstanceURLAttributeDTO();
            dto.setName(attr.getName());
            dto.setValue(attr.getValue());
            return dto;
        } else {
            throw new IllegalArgumentException("Instance attribute not supported");
        }
    }



    private SearchQuery.AbstractAttributeQuery[] createObject(SearchQueryDTO.AbstractAttributeQueryDTO[] dtos) {
        if (dtos == null) {
            return null;
        }
        SearchQuery.AbstractAttributeQuery[] data = new SearchQuery.AbstractAttributeQuery[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
        }

        return data;
    }

    private InstanceAttribute[] createObject(InstanceAttributeDTO[] dtos) {
        if (dtos == null) {
            return null;
        }
        InstanceAttribute[] data = new InstanceAttribute[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
        }

        return data;
    }

    private SearchQuery.AbstractAttributeQuery createObject(SearchQueryDTO.AbstractAttributeQueryDTO dto) {
        if (dto instanceof SearchQueryDTO.BooleanAttributeQueryDTO) {
            SearchQueryDTO.BooleanAttributeQueryDTO booleanDTO = (SearchQueryDTO.BooleanAttributeQueryDTO) dto;
            SearchQuery.BooleanAttributeQuery attr = new SearchQuery.BooleanAttributeQuery(booleanDTO.getName(),booleanDTO.isBooleanValue());
            return attr;
        } else if (dto instanceof SearchQueryDTO.TextAttributeQueryDTO) {
            SearchQueryDTO.TextAttributeQueryDTO textDTO = (SearchQueryDTO.TextAttributeQueryDTO) dto;
            SearchQuery.TextAttributeQuery attr = new SearchQuery.TextAttributeQuery(textDTO.getName(),textDTO.getTextValue());
            return attr;
        } else if (dto instanceof SearchQueryDTO.NumberAttributeQueryDTO) {
            SearchQueryDTO.NumberAttributeQueryDTO numberDTO = (SearchQueryDTO.NumberAttributeQueryDTO) dto;
            SearchQuery.NumberAttributeQuery attr = new SearchQuery.NumberAttributeQuery(numberDTO.getName(),numberDTO.getNumberValue());
            return attr;
        } else if (dto instanceof SearchQueryDTO.DateAttributeQueryDTO) {
            SearchQueryDTO.DateAttributeQueryDTO dateDTO = (SearchQueryDTO.DateAttributeQueryDTO) dto;
            SearchQuery.DateAttributeQuery attr = new SearchQuery.DateAttributeQuery(dateDTO.getName(),dateDTO.getFromDate(),dateDTO.getToDate());
            return attr;
        } else if (dto instanceof SearchQueryDTO.URLAttributeQueryDTO) {
            SearchQueryDTO.URLAttributeQueryDTO urlDTO = (SearchQueryDTO.URLAttributeQueryDTO) dto;
            SearchQuery.URLAttributeQuery attr = new SearchQuery.URLAttributeQuery(urlDTO.getName(),urlDTO.getUrlValue());
            return attr;
        } else {
            throw new IllegalArgumentException("Attribute query not supported");
        }
    }

    private InstanceAttribute createObject(InstanceAttributeDTO dto) {
        if (dto instanceof InstanceBooleanAttributeDTO) {
            InstanceBooleanAttribute attr = new InstanceBooleanAttribute();
            attr.setName(dto.getName());
            attr.setBooleanValue((Boolean) dto.getValue());
            return attr;
        } else if (dto instanceof InstanceTextAttributeDTO) {
            InstanceTextAttribute attr = new InstanceTextAttribute();
            attr.setName(dto.getName());
            attr.setTextValue((String) dto.getValue());
            return attr;
        } else if (dto instanceof InstanceNumberAttributeDTO) {
            InstanceNumberAttribute attr = new InstanceNumberAttribute();
            attr.setName(dto.getName());
            attr.setNumberValue((Float) dto.getValue());
            return attr;
        } else if (dto instanceof InstanceDateAttributeDTO) {
            InstanceDateAttribute attr = new InstanceDateAttribute();
            attr.setName(dto.getName());
            attr.setDateValue((Date) dto.getValue());
            return attr;
        } else if (dto instanceof InstanceURLAttributeDTO) {
            InstanceURLAttribute attr = new InstanceURLAttribute();
            attr.setName(dto.getName());
            attr.setUrlValue((String) dto.getValue());
            return attr;
        } else {
            throw new IllegalArgumentException("Instance attribute not supported");
        }
    }

    private InstanceAttributeTemplate[] createObject(InstanceAttributeTemplateDTO[] dtos) {
        InstanceAttributeTemplate[] data = new InstanceAttributeTemplate[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
        }

        return data;
    }

    private InstanceAttributeTemplate createObject(InstanceAttributeTemplateDTO dto) {
        InstanceAttributeTemplate data = new InstanceAttributeTemplate();
        data.setName(dto.getName());
        data.setAttributeType(InstanceAttributeTemplate.AttributeType.valueOf(dto.getAttributeType().name()));
        return data;
    }

    private DocumentKey[] createObject(DocumentDTO[] dtos) {
        DocumentKey[] data = new DocumentKey[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
        }

        return data;
    }

    private DocumentKey createObject(DocumentDTO dto) {
        return new DocumentKey(dto.getWorkspaceId(), dto.getMasterDocumentId(), dto.getMasterDocumentVersion(), dto.getIteration());
    }

    private ActivityModel[] createObject(ActivityModelDTO[] dtos) {
        ActivityModel[] data = new ActivityModel[dtos.length];

        for (int i = 0; i < dtos.length; i++) {
            data[i] = createObject(dtos[i]);
            data[i].setStep(i);
        }
        return data;
    }

    private ActivityModel createObject(ActivityModelDTO dto) {

        ActivityModel obj = null;
        if (dto instanceof SerialActivityModelDTO) {
            obj = new SerialActivityModel();
        } else {
            ParallelActivityModel tmp = new ParallelActivityModel();
            ParallelActivityModelDTO tmpDto = (ParallelActivityModelDTO) dto;
            tmp.setTasksToComplete(tmpDto.getTasksToComplete());
            obj = tmp;

        }

        int num = 0;
        List<TaskModel> tasks = new ArrayList<TaskModel>();
        for (TaskModelDTO task : dto.getTaskModels()) {
            TaskModel taskObj = createObject(task);
            taskObj.setNum(num++);
            tasks.add(taskObj);
        }
        obj.setTaskModels(tasks);
        obj.setLifeCycleState(dto.getLifeCycleState());
        return obj;
    }

    private TaskModel createObject(TaskModelDTO dto) {
        TaskModel obj = new TaskModel();
        obj.setTitle(dto.getTitle());
        obj.setWorker(createObject(dto.getWorker()));
        obj.setInstructions(dto.getInstructions());
        return obj;
    }

    private User createObject(UserDTO dto) {
        User obj = new User();
        obj.setLogin(dto.getLogin());
        obj.setName(dto.getName());
        obj.setEmail(dto.getEmail());
        obj.setWorkspace(new Workspace(dto.getWorkspaceId()));

        return obj;
    }

    private MasterDocumentDTO[] setupMDocNotifications(MasterDocumentDTO[] mdocs, String workspaceId) throws com.docdoku.core.services.ApplicationException{
            List<MasterDocumentDTO> markedIteration = Arrays.asList(createDTO(commandService.getIterationChangeEventSubscriptions(workspaceId)));
            List<MasterDocumentDTO> markedState = Arrays.asList(createDTO(commandService.getStateChangeEventSubscriptions(workspaceId)));

            for (int i = 0 ; i < mdocs.length ; i++){
                mdocs[i].setIterationSubscription(markedIteration.contains(mdocs[i]));
                mdocs[i].setStateSubscription(markedState.contains(mdocs[i]));
            }
            return mdocs ;
    }

    public MDocResponse getCheckedOutMDocs(String workspaceId, int startOffset, int chunkSize) throws ApplicationException {
        return generateMDocResponse(getCheckedOutMDocs(workspaceId), startOffset,chunkSize);   
    }

    public MDocResponse findMDocsByFolder(String completePath, int startOffset, int chunkSize) throws ApplicationException {
        return generateMDocResponse(findMDocsByFolder(completePath), startOffset, chunkSize);
    }

    public MDocResponse findMDocsByTag(String workspaceId, String label, int startOffset, int chunkSize) throws ApplicationException {
        return generateMDocResponse(findMDocsByTag(workspaceId, label), startOffset, chunkSize);
    }

    public MDocResponse searchMDocs(String workspaceId, String mdocId, String title, String version, String author, String type, Date creationDateFrom, Date creationDateTo, SearchQueryDTO.AbstractAttributeQueryDTO[] attributes, String[] tags, String content, int startOffset, int chunkSize) throws ApplicationException {
        return generateMDocResponse(searchMDocs(workspaceId, mdocId, title, version, author, type, creationDateFrom, creationDateTo, attributes, tags, content), startOffset, chunkSize);
    }
    

    private MDocResponse generateMDocResponse(MasterDocumentDTO response[], int startOffset, int chunkSize){
        if (startOffset < response.length){

            MasterDocumentDTO chunk[] ;

            if (startOffset + chunkSize <= response.length){
                chunk = new MasterDocumentDTO[chunkSize] ;
                int j = 0 ;
                for (int i = startOffset; i < startOffset + chunkSize; i++) {
                     chunk[j]= response[i];
                     j++;
                }
            }else{
                chunk = new MasterDocumentDTO[response.length - startOffset] ;
                int j = 0 ;
                for (int i = startOffset ; i < response.length ; i++){
                    chunk[j] = response[i] ;
                    j++ ;
                }
            }

            MDocResponse result = new MDocResponse();
            result.setChunckOffset(startOffset);
            result.setTotalSize(response.length);
            result.setData(chunk);
            return result;

        }

        MDocResponse defaultResponse = new MDocResponse() ;
        defaultResponse.setChunckOffset(0);
        defaultResponse.setTotalSize(0);
        MasterDocumentDTO defaultChunk[] = new MasterDocumentDTO[0] ;
        defaultResponse.setData(defaultChunk);

        return  defaultResponse;
    }
}
