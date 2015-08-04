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
package com.docdoku.server;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkflowManagerLocal;
import com.docdoku.core.services.IWorkflowManagerWS;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.*;
import com.docdoku.server.dao.*;
import com.docdoku.server.factory.ACLFactory;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@Local(IWorkflowManagerLocal.class)
@Stateless(name = "WorkflowManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IWorkflowManagerWS")
public class WorkflowManagerBean implements IWorkflowManagerWS, IWorkflowManagerLocal {

    @PersistenceContext
    private EntityManager em;

    //@PersistenceUnit
    //private EntityManagerFactory emf;

    @EJB
    private IUserManagerLocal userManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException, EntityConstraintException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        Locale locale = new Locale(user.getLanguage());
        WorkflowModelDAO workflowModelDAO = new WorkflowModelDAO(locale, em);

        WorkflowModel workflowModel = workflowModelDAO.loadWorkflowModel(pKey);

        if(workflowModelDAO.isInUseInDocumentMasterTemplate(workflowModel)){
            throw new EntityConstraintException(locale,"EntityConstraintException24");
        }

        if(workflowModelDAO.isInUseInPartMasterTemplate(workflowModel)){
            throw new EntityConstraintException(locale,"EntityConstraintException25");
        }

        checkWorkflowWriteAccess(workflowModel,user);

        workflowModelDAO.removeWorkflowModel(pKey);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, UserNotFoundException, WorkflowModelAlreadyExistsException, CreationException, NotAllowedException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());

        checkWorkflowValidity(pWorkspaceId, pId, userLocale, pActivityModels);

        WorkflowModelDAO modelDAO = new WorkflowModelDAO(userLocale, em);
        WorkflowModel model = new WorkflowModel(user.getWorkspace(), pId, user, pFinalLifeCycleState, pActivityModels);
        Tools.resetParentReferences(model);
        Date now = new Date();
        model.setCreationDate(now);
        modelDAO.createWorkflowModel(model);
        return model;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public WorkflowModel updateWorkflowModel(WorkflowModelKey workflowModelKey, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, AccessRightException, WorkflowModelNotFoundException, NotAllowedException, WorkflowModelAlreadyExistsException, CreationException {
        //remove all activities from workflow model
        //but do not remove it to maintain associated links
        //for instance document or part templates
        //and also ACL, author, creation date...
        User user = userManager.checkWorkspaceReadAccess(workflowModelKey.getWorkspaceId());
        Locale userLocale = new Locale(user.getLanguage());
        WorkflowModelDAO workflowModelDAO = new WorkflowModelDAO(userLocale, em);

        workflowModelDAO.removeAllActivityModels(workflowModelKey);

        WorkflowModel workflowModel = workflowModelDAO.loadWorkflowModel(workflowModelKey);
        checkWorkflowWriteAccess(workflowModel,user);

        checkWorkflowValidity(workflowModelKey.getWorkspaceId(), workflowModelKey.getId(), userLocale, pActivityModels);
        workflowModel.setFinalLifeCycleState(pFinalLifeCycleState);
        List<ActivityModel> activityModels = new LinkedList<>();
        Collections.addAll(activityModels, pActivityModels);
        workflowModel.setActivityModels(activityModels);
        Tools.resetParentReferences(workflowModel);
        return workflowModel;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);

        List<WorkflowModel> allWorkflowModels = new WorkflowModelDAO(new Locale(user.getLanguage()), em).findAllWorkflowModels(pWorkspaceId);

        ListIterator<WorkflowModel> ite = allWorkflowModels.listIterator();
        while(ite.hasNext()){
            WorkflowModel workflowModel = ite.next();
            if(!hasWorkflowModelReadAccess(workflowModel, user)){
                ite.remove();
            }
        }
        return allWorkflowModels.toArray(new WorkflowModel[allWorkflowModels.size()]);
    }

    private boolean hasWorkflowModelReadAccess(WorkflowModel workflowModel, User user) {
        return user.isAdministrator() || isACLGrantReadAccess(user,workflowModel);
    }

    private boolean isACLGrantReadAccess(User user, WorkflowModel workflowModel) {
        return workflowModel.getAcl()==null || workflowModel.getAcl().hasReadAccess(user);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public WorkflowModel getWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, WorkflowModelNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pKey.getWorkspaceId());
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(pKey);
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public Role[] getRoles(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()), em);
        List<Role> roles = roleDAO.findRolesInWorkspace(pWorkspaceId);
        return roles.toArray(new Role[roles.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Role[] getRolesInUse(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()), em);
        List<Role> roles = roleDAO.findRolesInUseWorkspace(pWorkspaceId);
        return roles.toArray(new Role[roles.size()]);
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public Role createRole(String roleName, String workspaceId, String userLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        Workspace wks = new WorkspaceDAO(locale, em).loadWorkspace(workspaceId);
        Role role = new Role(roleName, wks);

        if (userLogin != null) {
            User userMapped = new UserDAO(locale, em).loadUser(new UserKey(workspaceId, userLogin));
            role.setDefaultAssignee(userMapped);
        }

        new RoleDAO(locale, em).createRole(role);
        return role;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Role updateRole(RoleKey roleKey, String userLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(roleKey.getWorkspace());
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()), em);
        Role role = roleDAO.loadRole(roleKey);

        if (userLogin != null) {
            User userMapped = new UserDAO(new Locale(user.getLanguage()), em).loadUser(new UserKey(roleKey.getWorkspace(), userLogin));
            role.setDefaultAssignee(userMapped);
        } else {
            role.setDefaultAssignee(null);
        }

        return role;

    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public void deleteRole(RoleKey roleKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleNotFoundException, EntityConstraintException {
        User user = userManager.checkWorkspaceWriteAccess(roleKey.getWorkspace());
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()), em);
        Role role = roleDAO.loadRole(roleKey);

        if (roleDAO.isRoleInUseInWorkflowModel(role)) {
            throw new EntityConstraintException(new Locale(user.getLanguage()), "EntityConstraintException3");
        }

        roleDAO.deleteRole(role);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void removeACLFromWorkflow(String pWorkspaceId, String workflowModelId) throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkflowModelNotFoundException, AccessRightException {
        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Load the workflowModel
        WorkflowModelKey workflowModelKey = new WorkflowModelKey(pWorkspaceId, workflowModelId);
        WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(workflowModelKey);
        // Check the access to the workflow
        checkWorkflowWriteAccess(workflowModel, user);

        ACL acl = workflowModel.getAcl();
        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            workflowModel.setAcl(null);
        }
    }


    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public WorkflowModel updateACLForWorkflow(String pWorkspaceId, String workflowModelId, Map<String, String> userEntries, Map<String, String> groupEntries) throws WorkflowNotFoundException, UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, WorkflowModelNotFoundException, AccessRightException {
        // Check the read access to the workspace
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        // Load the workflowModel
        WorkflowModelKey workflowModelKey = new WorkflowModelKey(pWorkspaceId, workflowModelId);
        WorkflowModel workflowModel = new WorkflowModelDAO(new Locale(user.getLanguage()), em).loadWorkflowModel(workflowModelKey);
        // Check the access to the workflow
        checkWorkflowWriteAccess(workflowModel, user);
        ACLFactory aclFactory = new ACLFactory(em);

        if(workflowModel.getAcl() == null){
            ACL acl = aclFactory.createACL(pWorkspaceId, userEntries, groupEntries);
            workflowModel.setAcl(acl);
        }else{
            aclFactory.updateACL(pWorkspaceId,workflowModel.getAcl(),userEntries, groupEntries);
        }

        return workflowModel;


    }


    private void checkWorkflowValidity(String workspaceId, String pId, Locale userLocale, ActivityModel[] pActivityModels) throws NotAllowedException {

        List<Role> roles = new RoleDAO(userLocale, em).findRolesInWorkspace(workspaceId);

        if (pId == null || " ".equals(pId)) {
            throw new NotAllowedException(userLocale, "WorkflowNameEmptyException");
        }

        if (pActivityModels.length == 0) {
            throw new NotAllowedException(userLocale, "NotAllowedException2");
        }

        for (ActivityModel activity : pActivityModels) {
            if (activity.getLifeCycleState() == null || "".equals(activity.getLifeCycleState()) || activity.getTaskModels().isEmpty()) {
                throw new NotAllowedException(userLocale, "NotAllowedException3");
            }
            for (TaskModel taskModel : activity.getTaskModels()) {

                Role modelRole = taskModel.getRole();
                if (modelRole == null) {
                    throw new NotAllowedException(userLocale, "NotAllowedException13");
                }
                String roleName = modelRole.getName();
                for (Role role : roles) {
                    if (role.getName().equals(roleName)) {
                        taskModel.setRole(role);
                        break;
                    }
                }
            }
        }
    }



    private User checkWorkflowWriteAccess(WorkflowModel workflow, User user) throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException {
        if (user.isAdministrator()) {
            // Check if it is the workspace's administrator
            return user;
        }
        if (workflow.getAcl() == null) {
            // Check if the item haven't ACL
            return userManager.checkWorkspaceWriteAccess(workflow.getWorkspaceId());
        } else if (workflow.getAcl().hasWriteAccess(user)) {
            // Check if there is a write access
            return user;
        } else {
            // Else throw a AccessRightException
            throw new AccessRightException(new Locale(user.getLanguage()), user);
        }
    }

}