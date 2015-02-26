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
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.services.IWorkflowManagerLocal;
import com.docdoku.core.services.IWorkflowManagerWS;
import com.docdoku.core.util.Tools;
import com.docdoku.core.workflow.*;
import com.docdoku.server.dao.RoleDAO;
import com.docdoku.server.dao.UserDAO;
import com.docdoku.server.dao.WorkflowModelDAO;
import com.docdoku.server.dao.WorkspaceDAO;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@DeclareRoles(UserGroupMapping.REGULAR_USER_ROLE_ID)
@Local(IWorkflowManagerLocal.class)
@Stateless(name = "WorkflowManagerBean")
@WebService(endpointInterface = "com.docdoku.core.services.IWorkflowManagerWS")
public class WorkflowManagerBean implements IWorkflowManagerWS, IWorkflowManagerLocal {

    @PersistenceContext
    private EntityManager em;
    @EJB
    private IUserManagerLocal userManager;

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public void deleteWorkflowModel(WorkflowModelKey pKey) throws WorkspaceNotFoundException, AccessRightException, WorkflowModelNotFoundException, UserNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(pKey.getWorkspaceId());
        // TODO: check if a template exists with this workflowmodel and show exception "metier"
        new WorkflowModelDAO(new Locale(user.getLanguage()), em).removeWorkflowModel(pKey);
        em.flush();
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public WorkflowModel createWorkflowModel(String pWorkspaceId, String pId, String pFinalLifeCycleState, ActivityModel[] pActivityModels) throws WorkspaceNotFoundException, AccessRightException, UserNotFoundException, WorkflowModelAlreadyExistsException, CreationException, NotAllowedException {
        User user = userManager.checkWorkspaceWriteAccess(pWorkspaceId);
        Locale userLocale = new Locale(user.getLanguage());

        checkWorkflowValidity(pWorkspaceId,userLocale,pId,pActivityModels);

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
    public WorkflowModel[] getWorkflowModels(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        return new WorkflowModelDAO(new Locale(user.getLanguage()), em).findAllWorkflowModels(pWorkspaceId);
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
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()),em);
        List<Role> roles = roleDAO.findRolesInWorkspace(pWorkspaceId);
        return roles.toArray(new Role[roles.size()]);
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Role[] getRolesInUse(String pWorkspaceId) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException {
        User user = userManager.checkWorkspaceReadAccess(pWorkspaceId);
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()),em);
        List<Role> roles = roleDAO.findRolesInUseWorkspace(pWorkspaceId);
        return roles.toArray(new Role[roles.size()]);
    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public Role createRole(String roleName, String workspaceId, String userLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleAlreadyExistsException, CreationException {
        User user = userManager.checkWorkspaceWriteAccess(workspaceId);
        Locale locale = new Locale(user.getLanguage());
        Workspace wks = new WorkspaceDAO(locale, em).loadWorkspace(workspaceId);
        Role role = new Role(roleName,wks);

        if(userLogin != null){
            User userMapped = new UserDAO(locale,em).loadUser(new UserKey(workspaceId,userLogin));
            role.setDefaultUserMapped(userMapped);
        }

        new RoleDAO(locale,em).createRole(role);
        return role;
    }

    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    @Override
    public Role updateRole(RoleKey roleKey, String userLogin) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleNotFoundException {
        User user = userManager.checkWorkspaceWriteAccess(roleKey.getWorkspace());
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()),em);
        Role role = roleDAO.loadRole(roleKey);

        if(userLogin != null){
            User userMapped = new UserDAO(new Locale(user.getLanguage()),em).loadUser(new UserKey(roleKey.getWorkspace(),userLogin));
            role.setDefaultUserMapped(userMapped);
        }else{
            role.setDefaultUserMapped(null);
        }

        return role;

    }

    @Override
    @RolesAllowed(UserGroupMapping.REGULAR_USER_ROLE_ID)
    public void deleteRole(RoleKey roleKey) throws WorkspaceNotFoundException, UserNotFoundException, UserNotActiveException, AccessRightException, RoleNotFoundException, EntityConstraintException {
        User user = userManager.checkWorkspaceWriteAccess(roleKey.getWorkspace());
        RoleDAO roleDAO = new RoleDAO(new Locale(user.getLanguage()),em);
        Role role = roleDAO.loadRole(roleKey);

        if(roleDAO.isRoleInUseInWorkflowModel(role)){
            throw new EntityConstraintException(new Locale(user.getLanguage()),"EntityConstraintException3");
        }

        roleDAO.deleteRole(role);
    }


    private void checkWorkflowValidity(String workspaceId, Locale userLocale,String pId,ActivityModel[] pActivityModels) throws NotAllowedException {
       
        List<Role> roles = new RoleDAO(userLocale,em).findRolesInWorkspace(workspaceId);
        
        if(pId == null || " ".equals(pId)){
            throw new NotAllowedException(userLocale,"WorkflowNameEmptyException");
        }

        if(pActivityModels.length==0){
            throw new NotAllowedException(userLocale,"NotAllowedException2");
        }

        for(ActivityModel activity : pActivityModels){
            if(activity.getLifeCycleState()==null || "".equals(activity.getLifeCycleState() ) || activity.getTaskModels().isEmpty()){
                throw new NotAllowedException(userLocale,"NotAllowedException3");
            }
            for (TaskModel taskModel : activity.getTaskModels()) {

                Role modelRole = taskModel.getRole();
                if(modelRole==null){
                    throw new NotAllowedException(userLocale,"NotAllowedException13");
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
    
}