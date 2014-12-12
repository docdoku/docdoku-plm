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

package com.docdoku.arquillian.tests;

import com.docdoku.arquillian.tests.services.TestDocumentManagerBean;
import com.docdoku.arquillian.tests.services.TestUserManagerBean;
import com.docdoku.arquillian.tests.services.TestWorkflowManagerBean;
import com.docdoku.arquillian.tests.util.TestUtil;
import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.common.*;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.core.workflow.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class WorkflowTest {

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @EJB
    private TestWorkflowManagerBean workflowManagerBean;

    @EJB
    private ESIndexer esIndexer;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private static final int COUNT = 5;

    private  TestUtil util =new TestUtil();

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests-workflow.war")
                .addPackage(Workspace.class.getPackage())
                .addClasses(
                        Account.class,
                        ACLUserGroupEntry.class,
                        ACLUserEntry.class,
                        Activity.class,
                        ActivityModel.class,
                        InstanceAttributeTemplate.class,
                        Organization.class,
                        Credential.class,
                        ChangeItem.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IWorkflowManagerLocal.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        IUserManagerLocal.class,
                        IWorkspaceManagerLocal.class,
                        InstanceAttributeTemplate.class,
                        JsonValue.class,
                        MailerBean.class,
                        PartMasterTemplate.class,
                        PartMaster.class,
                        ParallelActivityModel.class,
                        Role.class,
                        SerialActivityModel.class,
                        TaskModel.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        TestWorkflowManagerBean.class,
                        UserManagerBean.class,
                        Workspace.class,
                        WorkflowModel.class,
                        Workflow.class,
                        WorkflowManagerBean.class,
                        WorkspaceManagerBean.class,
                        WorkspaceUserMembership.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("WEB-INF/sun-web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void insertData() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.clear();
        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date());
            em.merge(Credential.createCredential(account.getLogin(), "password"));
            em.merge(new UserGroupMapping(account.getLogin()));
            em.merge(account);
        }
        utx.commit();

        util.init(userManagerBean,documentManagerBean);

    }


    @Test
    public void create_workflow_with_no_activity() throws AccountNotFoundException, ESIndexNamingException, WorkspaceAlreadyExistsException, NotAllowedException, CreationException, FolderAlreadyExistsException, UserAlreadyExistsException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, WorkflowModelAlreadyExistsException, FolderNotFoundException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : create_workflow_with_no_activity    ");
        try {
            WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", new ActivityModel[0]);
        } catch (NotAllowedException ignored) {
        } finally {
            assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 0);
        }

    }

    @Test
    public void create_workflowModel() throws CreationException, FolderAlreadyExistsException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, FolderNotFoundException, WorkflowModelAlreadyExistsException, AccessRightException, UserNotActiveException, RoleAlreadyExistsException, RoleNotFoundException, EntityConstraintException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : create_workflowModel");

        Map<Role, User> roles = new HashMap<>();
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role1", TestUtil.WORKSPACE_TEST, TestUtil.USER1_TEST), new User(TestUtil.USER1_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role2", TestUtil.WORKSPACE_TEST, TestUtil.USER2_TEST), new User(TestUtil.USER2_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role3", TestUtil.WORKSPACE_TEST, TestUtil.USER3_TEST), new User(TestUtil.USER3_TEST));

        ActivityModel[] sactivityModels = new SerialActivityModel[1];
        sactivityModels[0] = new SerialActivityModel();
        sactivityModels[0].createActivity(roles);
        sactivityModels[0].setLifeCycleState("etat1");
        sactivityModels[0].setStep(1);
        sactivityModels[0].addTaskModel(new TaskModel(sactivityModels[0], 1, "titleTask", "", roles.keySet().iterator().next()));

        WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", sactivityModels);

        assertTrue(workflowModel != null);
        assertTrue(!(workflowModel.getActivityModels().get(0) instanceof ParallelActivityModel));
        assertTrue(workflowModel.getActivityModels().get(0) instanceof SerialActivityModel);
        assertTrue(workflowManagerBean.getWorkspaceRoles(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 3);

    }


    @Test
    public void create_workflow_with_activity_without_tasks() throws CreationException, FolderAlreadyExistsException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, FolderNotFoundException, WorkflowModelAlreadyExistsException, AccessRightException, UserNotActiveException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : create_workflow_with_activity_without_tasks");
        ActivityModel[] sactivityModels = new SerialActivityModel[1];
        sactivityModels[0] = new SerialActivityModel();
        sactivityModels[0].setLifeCycleState("etat1");
        sactivityModels[0].setStep(1);
        sactivityModels[0].setTaskModels(null);
        workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", sactivityModels);
        Assert.assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 0);

    }

    @Test
    public void create_workflow_with_task_having_no_roles() throws CreationException, FolderAlreadyExistsException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, FolderNotFoundException, WorkflowModelAlreadyExistsException, AccessRightException, UserNotActiveException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : create_workflow_with_task_having_no_roles");
        ActivityModel[] sactivityModels = new SerialActivityModel[1];
        sactivityModels[0] = new SerialActivityModel();
        sactivityModels[0].setLifeCycleState("etat1");
        sactivityModels[0].setStep(1);
        List<TaskModel> taskModels = new ArrayList<TaskModel>();
        taskModels.add(new TaskModel());
        taskModels.get(0).createTask(new HashMap<Role, User>(0));
        sactivityModels[0].setTaskModels(taskModels);
        try {
            workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", sactivityModels);
        } catch (Exception e) {
        } finally {
            Assert.assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 0);
        }

    }


    @Test
    public void create_parallel_activity() throws CreationException, FolderAlreadyExistsException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, FolderNotFoundException, WorkflowModelAlreadyExistsException, AccessRightException, UserNotActiveException, RoleAlreadyExistsException, EntityConstraintException, RoleNotFoundException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : create_parallel_activity");
        Map<Role, User> roles = new HashMap<>();
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role1", TestUtil.WORKSPACE_TEST, TestUtil.USER1_TEST), new User(TestUtil.USER1_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role2", TestUtil.WORKSPACE_TEST, TestUtil.USER2_TEST), new User(TestUtil.USER2_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role3", TestUtil.WORKSPACE_TEST, TestUtil.USER3_TEST), new User(TestUtil.USER3_TEST));

        ParallelActivityModel[] activityModels = new ParallelActivityModel[1];
        activityModels[0] = new ParallelActivityModel();
        activityModels[0].setLifeCycleState("state1");
        activityModels[0].setTaskModels(new ArrayList<TaskModel>());
        activityModels[0].createActivity(roles);
        TaskModel task = new TaskModel(activityModels[0], 1, "titleTask", "", roles.keySet().iterator().next());
        task.createTask(roles);
        activityModels[0].addTaskModel(task);
        activityModels[0].setTasksToComplete(3);
        WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", activityModels);
        assertTrue(workflowModel != null);
        assertTrue(workflowManagerBean.getWorkspaceRoles(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 3);

        Assert.assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 1);
        Assert.assertTrue(((ParallelActivityModel)workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST)[0].getActivityModels().get(0)).getTasksToComplete() == 1);

    }

    @Test
    public void create_workflow_with_empty_name() throws RoleNotFoundException, UserNotActiveException, WorkspaceNotFoundException, CreationException, UserNotFoundException, EntityConstraintException, AccessRightException, RoleAlreadyExistsException, NotAllowedException, WorkflowModelAlreadyExistsException, FolderAlreadyExistsException, FolderNotFoundException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : create_workflow_with_empty_name");
        Map<Role, User> roles = new HashMap<>();
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role1", TestUtil.WORKSPACE_TEST, TestUtil.USER1_TEST), new User(TestUtil.USER1_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role2", TestUtil.WORKSPACE_TEST, TestUtil.USER2_TEST), new User(TestUtil.USER2_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role3", TestUtil.WORKSPACE_TEST, TestUtil.USER3_TEST), new User(TestUtil.USER3_TEST));

        ActivityModel[] sactivityModels = new SerialActivityModel[1];
        sactivityModels[0] = new SerialActivityModel();
        sactivityModels[0].createActivity(roles);
        sactivityModels[0].setLifeCycleState("etat1");
        sactivityModels[0].setStep(1);
        sactivityModels[0].addTaskModel(new TaskModel(sactivityModels[0], 1, "titleTask", "", roles.keySet().iterator().next()));

        try {
            WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "", "", sactivityModels);
        }
        catch (Exception e){}
        finally {
            Assert.assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST).length == 0);
        }

    }

    @Test
    public void duplicate_role() throws WorkspaceNotFoundException, UserNotActiveException, UserNotFoundException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : duplicate_role");
        try {
            workflowManagerBean.createRole(TestUtil.USER1_TEST, "role1", TestUtil.WORKSPACE_TEST, TestUtil.USER1_TEST);
            workflowManagerBean.createRole(TestUtil.USER1_TEST, "role1", TestUtil.WORKSPACE_TEST, TestUtil.USER2_TEST);

        } catch (Exception e) { }
        finally {
            assertTrue(workflowManagerBean.getWorkspaceRoles(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 1);
        }

    }


}