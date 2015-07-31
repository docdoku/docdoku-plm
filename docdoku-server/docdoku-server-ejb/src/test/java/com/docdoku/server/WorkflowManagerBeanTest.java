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

import com.docdoku.core.common.*;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.security.ACL;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.workflow.WorkflowModelKey;
import com.docdoku.server.util.WorkflowUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.MockitoAnnotations.initMocks;

public class WorkflowManagerBeanTest {


    @InjectMocks
    WorkflowManagerBean workflowManagerBean = new WorkflowManagerBean();

    @Mock
    private EntityManager em;
    @Mock
    private IUserManagerLocal userManager;
    @Mock
    TypedQuery<ACL> aclTypedQuery;
    @Mock
    StoredProcedureQuery storedProcedureQuery;

    private User user;
    private Account account;
    private Workspace workspace;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = new Account(WorkflowUtil.ADMIN_LOGIN, WorkflowUtil.ADMIN_NAME, WorkflowUtil.ADMIN_MAIL, "en", new Date(), null);
        workspace = new Workspace(WorkflowUtil.WORKSPACE_ID, account, WorkflowUtil.WORKSPACE_DESCRIPTION, false);
        user = new User(workspace,WorkflowUtil.USER_LOGIN , WorkflowUtil.USER_NAME,WorkflowUtil.USER_MAIL, "en");
    }

    /**
     * test the remove of acl from a workflow operated by user who doesn't have write access to the workflow
     * @throws Exception
     */
    @Test(expected = AccessRightException.class)
    public void testRemoveACLFromWorkflow() throws Exception {

        //Given
        WorkflowModel workflowModel = new WorkflowModel(workspace, WorkflowUtil.WORKSPACE_ID, user, "");
        ACL acl = new ACL();
        acl.addEntry(user, ACL.Permission.READ_ONLY);
        workflowModel.setAcl(acl);
        // User has read access to the workspace
        Mockito.when(userManager.checkWorkspaceReadAccess(WorkflowUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(WorkflowModel.class, new WorkflowModelKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.WORKFLOW_MODEL_ID))).thenReturn(workflowModel);
        //When
        workflowManagerBean.removeACLFromWorkflow(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID);
        //Then, removeACLFromWorkflow should throw AccessRightException, user doesn't have write access to the workflow
    }

    /**
     * create an ACL of the workflow, user is the admin of the workspace
     * @throws Exception
     */
    @Test
    public void testUpdateACLForWorkflowWithNoACL() throws Exception {

        //Given
        WorkflowModel workflowModel = new WorkflowModel(workspace, WorkflowUtil.WORKSPACE_ID, user, "");
        Map<String, String> userEntries = new HashMap<>();
        User user2 = new User(workspace,WorkflowUtil.USER2_LOGIN , WorkflowUtil.USER2_NAME,WorkflowUtil.USER2_MAIL, "en");
        User user3 = new User(workspace,WorkflowUtil.USER3_LOGIN , WorkflowUtil.USER3_NAME,WorkflowUtil.USER3_MAIL, "en");
        userEntries.put(user.getLogin(), ACL.Permission.FORBIDDEN.name());
        userEntries.put(user2.getLogin(), ACL.Permission.READ_ONLY.name());
        userEntries.put(user3.getLogin(), ACL.Permission.FULL_ACCESS.name());

        // User has read access to the workspace
        Mockito.when(userManager.checkWorkspaceReadAccess(WorkflowUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(WorkflowModel.class, new WorkflowModelKey(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID))).thenReturn(workflowModel);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.USER_LOGIN))).thenReturn(user);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER2_LOGIN))).thenReturn(user2);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER3_LOGIN))).thenReturn(user3);

        //When
        WorkflowModel workflow= workflowManagerBean.updateACLForWorkflow(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID, userEntries, null);
        //Then
        Assert.assertEquals(workflow.getAcl().getGroupEntries().size() ,0 );
        Assert.assertEquals(workflow.getAcl().getUserEntries().size() , 3);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user).getPermission() , ACL.Permission.FORBIDDEN);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user2).getPermission() , ACL.Permission.READ_ONLY);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user3).getPermission() , ACL.Permission.FULL_ACCESS);


    }

    @Test
    public void testUpdateACLForWorkflowWithAnExistingACL() throws Exception {
        //Given
        Map<String, String> userEntries = new HashMap<>();
        Map<String, String> grpEntries = new HashMap<>();
        User user2 = new User(workspace,WorkflowUtil.USER2_LOGIN , WorkflowUtil.USER2_NAME,WorkflowUtil.USER2_MAIL, "en");
        User user3 = new User(workspace,WorkflowUtil.USER3_LOGIN , WorkflowUtil.USER3_NAME,WorkflowUtil.USER3_MAIL, "en");
        UserGroup group1 = new UserGroup(workspace,WorkflowUtil.GRP1_ID);

        WorkflowModel workflowModel = new WorkflowModel(workspace, WorkflowUtil.WORKSPACE_ID, user, "");
        ACL acl = new ACL();
        // user2 had READ_ONLY access in the existing acl
        acl.addEntry(user2, ACL.Permission.READ_ONLY);
        acl.addEntry(group1, ACL.Permission.FULL_ACCESS);
        workflowModel.setAcl(acl);

        userEntries.put(user.getLogin(), ACL.Permission.FORBIDDEN.name());
        // user2 has non access FORBIDDEN in the new acl
        userEntries.put(user2.getLogin(), ACL.Permission.FORBIDDEN.name());
        userEntries.put(user3.getLogin(), ACL.Permission.FULL_ACCESS.name());


        //user2 belong to group1
        group1.addUser(user2);
        group1.addUser(user);
        //group1 has FULL_ACCESS
        grpEntries.put(group1.getId(),ACL.Permission.FULL_ACCESS.name());


        // User has read access to the workspace
        Mockito.when(userManager.checkWorkspaceReadAccess(WorkflowUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(WorkflowModel.class, new WorkflowModelKey(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID))).thenReturn(workflowModel);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER_LOGIN))).thenReturn(user);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER2_LOGIN))).thenReturn(user2);
        Mockito.when(em.find(User.class, new UserKey(WorkflowUtil.WORKSPACE_ID,WorkflowUtil.USER3_LOGIN))).thenReturn(user3);
        Mockito.when(em.getReference(UserGroup.class, new UserGroupKey(WorkflowUtil.WORKSPACE_ID, group1.getId()))).thenReturn(group1);
        Mockito.when(em.getReference(User.class, user.getKey())).thenReturn(user);
        Mockito.when(em.getReference(User.class, user2.getKey())).thenReturn(user2);
        Mockito.when(em.getReference(User.class, user3.getKey())).thenReturn(user3);
        Mockito.when(aclTypedQuery.setParameter(Matchers.anyString(),Matchers.any())).thenReturn(aclTypedQuery);
        Mockito.when(em.createNamedQuery(Matchers.<String>any())).thenReturn(aclTypedQuery);

          //When
        WorkflowModel workflow= workflowManagerBean.updateACLForWorkflow(WorkflowUtil.WORKSPACE_ID, WorkflowUtil.WORKFLOW_MODEL_ID, userEntries, grpEntries);
        //Then
        Assert.assertEquals(workflow.getAcl().getGroupEntries().size(),1 );
        Assert.assertEquals(workflow.getAcl().getUserEntries().size() , 3);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user).getPermission() , ACL.Permission.FORBIDDEN);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user2).getPermission() , ACL.Permission.FORBIDDEN);
        Assert.assertEquals(workflow.getAcl().getUserEntries().get(user3).getPermission() , ACL.Permission.FULL_ACCESS);

    }


}