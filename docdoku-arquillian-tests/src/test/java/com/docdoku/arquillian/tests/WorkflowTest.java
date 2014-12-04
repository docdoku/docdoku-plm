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

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests.war")
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
        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date());
            em.merge(Credential.createCredential(account.getLogin(), "password"));
            em.merge(new UserGroupMapping(account.getLogin()));
            em.merge(account);
        }
        utx.commit();

        userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
        userManagerBean.testWorkspaceCreation(TestUtil.USER2_TEST, TestUtil.WORKSPACE_TEST);
        userManagerBean.testWorkspaceCreation(TestUtil.USER3_TEST, TestUtil.WORKSPACE_TEST);

    }


    @Test
    public void
    test1_Workflow_ActivityValidity() throws AccountNotFoundException, ESIndexNamingException, WorkspaceAlreadyExistsException, NotAllowedException, CreationException, FolderAlreadyExistsException, UserAlreadyExistsException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException, UserNotFoundException, WorkflowModelAlreadyExistsException, FolderNotFoundException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Workflow_ActivityValidity");
        try {
            WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", new ActivityModel[0]);
        } catch (NotAllowedException ignored) {
        } finally {
            assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 0);
        }

    }

    @Test
    public void test2_WorkflowValidity() throws CreationException, FolderAlreadyExistsException, WorkspaceNotFoundException, UserNotFoundException, NotAllowedException, FolderNotFoundException, WorkflowModelAlreadyExistsException, AccessRightException, UserNotActiveException, RoleAlreadyExistsException, RoleNotFoundException, EntityConstraintException {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : WorkflowCreationWithRoles");

        Map<Role, User> roles = new HashMap<>();
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role1", TestUtil.WORKSPACE_TEST, TestUtil.USER1_TEST), new User(TestUtil.USER1_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role2", TestUtil.WORKSPACE_TEST, TestUtil.USER2_TEST), new User(TestUtil.USER2_TEST));
        roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST, "role3", TestUtil.WORKSPACE_TEST, TestUtil.USER3_TEST), new User(TestUtil.USER3_TEST));

        List<SerialActivityModel> sactivityModels = new ArrayList<SerialActivityModel>();
        SerialActivityModel serialActivityModel = new SerialActivityModel();
        serialActivityModel.createActivity(roles);
        sactivityModels.add(serialActivityModel);
        WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "workflow1", "", (ActivityModel[]) sactivityModels.toArray());
        assertTrue(workflowModel == null);
        assertTrue(workflowModel.getActivityModels().get(0) instanceof ParallelActivityModel);
        assertTrue(workflowModel.getActivityModels().get(0) instanceof ActivityModel);
        assertTrue(workflowManagerBean.getWorkspaceRoles(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST).length == 3);

    }


    @Test
    public void test3_Task_RoleValidity(){
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Task_RoleValidity");
        try{
            userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
            Map<Role,User> roles = new HashMap<>();
            roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST,"role1",TestUtil.WORKSPACE_TEST,TestUtil.USER1_TEST),new User(TestUtil.USER1_TEST));
            roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST,"role2",TestUtil.WORKSPACE_TEST,TestUtil.USER2_TEST),new User(TestUtil.USER2_TEST));
            roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST,"role3",TestUtil.WORKSPACE_TEST,TestUtil.USER3_TEST),new User(TestUtil.USER3_TEST));
            List<ActivityModel> sactivityModels = new ArrayList<ActivityModel>();
            SerialActivityModel serialActivityModel = new SerialActivityModel();
            serialActivityModel.createActivity(roles);
            sactivityModels.add(serialActivityModel);
            workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST, "workflow1", "", (ActivityModel[])sactivityModels.toArray());
        }
        catch (Exception e){
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Task_RoleValidity: Task without role" + e);
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Task_RoleValidity Stack trace" + e);
        }

    }
    @Test
    public void test4_Activity_TaskValidity(){
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Activity_TaskValidity");
        try{
            userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, "WORKSPACE2");
            Map<Role,User> roles = new HashMap<>();
            TaskModel task = null;
            task.createTask(roles);
            List<ActivityModel> sactivityModels = new ArrayList<ActivityModel>();
            SerialActivityModel serialActivityModel = new SerialActivityModel();
            serialActivityModel.createActivity(roles);
            serialActivityModel.addTaskModel(task);
            sactivityModels.add(serialActivityModel);

            WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST,"WORKSPACE2", "workflow1", "", (ActivityModel[])sactivityModels.toArray());
        }
        catch (Exception e){
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Activity_TaskValidity: Activity without task" + e);
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Activity_TaskValidity Stack trace" + e);
        }
    }
    @Test
    public void test5_Activity_ParallelTasksValidity(){
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Activity_ParallelTasksValidity");
        try{
            Workspace workspace = userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
            Map<Role,User> roles = new HashMap<>();
            roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST,"role1",TestUtil.WORKSPACE_TEST,TestUtil.USER1_TEST),new User(TestUtil.USER1_TEST));
            roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST,"role2",TestUtil.WORKSPACE_TEST,TestUtil.USER2_TEST),new User(TestUtil.USER2_TEST));
            roles.put(workflowManagerBean.createRole(TestUtil.USER1_TEST,"role3",TestUtil.WORKSPACE_TEST,TestUtil.USER3_TEST),new User(TestUtil.USER3_TEST));
            TaskModel task = null;
            task.createTask(roles);
            List<ActivityModel> activityModels = new ArrayList<ActivityModel>();
            ParallelActivityModel parallelActivityModel = new ParallelActivityModel();
            parallelActivityModel.createActivity(roles);
            parallelActivityModel.addTaskModel(task);
            parallelActivityModel.setTasksToComplete(3);
            activityModels.add(parallelActivityModel);

            WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST, "workflow1", "", (ActivityModel[])activityModels.toArray());
            assertTrue(workflowModel == null);
            assertTrue(workflowManagerBean.getWorkspaceRoles(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST).length == 3);
        }
        catch (Exception e){
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Activity_ParallelTasksValidity: number of tasks to complete is higher than defined tasks" + e);
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Activity_ParallelTasksValidity Stack trace" + e);
        }
    }

    @Test
    public void test6_Workflow_NameValidity() {
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Workflow_ActivityValidity");
        try{
            userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
            WorkflowModel workflowModel = workflowManagerBean.createWorkflowModel(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST, "", "", null);
            assertTrue(workflowModel == null);
        }
        catch (Exception e) {
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : WorkflowCreationWithoutRoles undefined name for workflow " + e);
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : WorkflowCreationWithoutRoles Stack trace" + e);
            try {
                assertTrue(workflowManagerBean.getWorkflows(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).length == 0);
            } catch (Exception exp) {
            }
        }
    }
    @Test
    public void test7_Task_RoleNameValidity(){
        Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Task_RoleNameValidity");
        try{
            userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
            workflowManagerBean.createRole(TestUtil.USER1_TEST,"role1",TestUtil.WORKSPACE_TEST,TestUtil.USER1_TEST);
            workflowManagerBean.createRole(TestUtil.USER1_TEST,"role1",TestUtil.WORKSPACE_TEST,TestUtil.USER2_TEST);


        }
        catch (Exception e){
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Task_RoleNameValidity: Task with the same name" + e);
            Logger.getLogger(WorkflowTest.class.getName()).log(Level.INFO, "Test method : Task_RoleNameValidity Stack trace" + e);
        }

    }
}