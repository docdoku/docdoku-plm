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

package com.docdoku.test.arquillian;

import com.docdoku.core.change.ChangeIssue;
import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.common.*;
import com.docdoku.core.configuration.BaselineCreation;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.core.workflow.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;
import com.docdoku.server.products.ProductBaselineManagerBean;
import com.docdoku.test.arquillian.services.*;
import com.docdoku.test.arquillian.util.TestUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Asmae CHADID
 */
@RunWith(Arquillian.class)
public class AccessRightsTest {

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    private static final int COUNT = 5;

    private TestUtil util =new TestUtil();

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests-acl.war")
                .addPackage(Workspace.class.getPackage())
                .addClasses(
                        Account.class,
                        ACLUserEntry.class,
                        ACLUserGroupEntry.class,
                        Activity.class,
                        ActivityModel.class,
                        BaselineCreation.class,
                        Organization.class,
                        Credential.class,
                        ChangeOrder.class,
                        ChangeRequest.class,
                        ChangeIssue.class,
                        ChangeManagerBean.class,
                        ChangeOrder.class,
                        ChangeRequest.class,
                        ChangeIssue.class,
                        ConfigurationItemKey.class,
                        ChangeOrder.class,
                        ConfigurationItem.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        DocumentIterationKey.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IChangeManagerLocal.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IProductManagerLocal.class,
                        IProductBaselineManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        InstanceAttributeTemplate.class,
                        ParallelActivityModel.class,
                        ProductManagerBean.class,
                        ProductBaselineManagerBean.class,
                        Role.class,
                        ProductBaseline.class,
                        SerialActivityModel.class,
                        TaskModel.class,
                        IUserManagerLocal.class,
                        IUploadDownloadWS.class,
                        IWorkspaceManagerLocal.class,
                        IWorkflowManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        PartMaster.class,
                        PartMasterTemplate.class,
                        PartRevision.class,
                        PartRevisionKey.class,
                        PartUsageLink.class,
                        ProductManagerBean.class,
                        ProductManagerBean.class,
                        ProductBaselineManagerBean.class,
                        ProductBaseline.class,
                        ChangeManagerBean.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        TestChangeManagerBean.class,
                        TestPartManagerBean.class,
                        TestProductManagerBean.class,
                        TestUploadDownloadManagerBean.class,
                        UserManagerBean.class,
                        Workspace.class,
                        WorkspaceManagerBean.class,
                        WorkspaceUserMembership.class,
                        WorkflowModel.class,
                        Workflow.class,
                        WorkflowManagerBean.class
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
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date(),null);
            em.merge(Credential.createCredential(account.getLogin(), "password"));
            em.merge(new UserGroupMapping(account.getLogin()));
            em.merge(account);
        }
        utx.commit();
        util.init(userManagerBean,documentManagerBean);
    }



    @Test
    public void create_document() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : create_document");
        userManagerBean.testAddingUserInWorkspace("user1", "user2", "TEST_WORKSPACE");
        documentManagerBean.createDocumentMaster("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT0", null, null);

    }

    @Test
    public void matrix_rights1() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : matrix_rights1");
        userManagerBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user2"}, "TEST_WORKSPACE", false);
        documentManagerBean.createDocumentMaster("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT1", null, null);
    }

    @Test
    public void matrix_rights2() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : matrix_rights2");
        userManagerBean.testGroupCreation("user1", "TEST_WORKSPACE", "group1");
        userManagerBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group1"}, "TEST_WORKSPACE", true);
        userManagerBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
        userManagerBean.testAddingUserInWorkspace("user1", "user3", "TEST_WORKSPACE");
        userManagerBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user3"}, "TEST_WORKSPACE", false);
        documentManagerBean.createDocumentMaster("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT3", null, null);
    }

    @Test
    public void matrix_rights3() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : matrix_rights3");
        userManagerBean.testGroupCreation("user1", "TEST_WORKSPACE", "group2");
        userManagerBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group2"}, "TEST_WORKSPACE", false);
        userManagerBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group1"}, "TEST_WORKSPACE", true);
        userManagerBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
        userManagerBean.testAddingUserInGroup("user1", "group2", "TEST_WORKSPACE", "user3");
        documentManagerBean.createDocumentMaster("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT4", null, null);
    }

    @Test
    public void matrix_rights4() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : matrix_rights4");
        userManagerBean.testAddingUserInWorkspace("user1", "user4", "TEST_WORKSPACE");
        userManagerBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user4"}, "TEST_WORKSPACE", true);

    }

    @Test
    public void checkIn_checkOut_document() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : checkIn_checkOut_document");
        ACLUserGroupEntry[] aclUserGroupEntries = new ACLUserGroupEntry[1];
        UserGroup userGroup = em.find(UserGroup.class, new UserGroupKey("TEST_WORKSPACE", "group1"));
        aclUserGroupEntries[0] = new ACLUserGroupEntry(new ACL(), userGroup, ACL.Permission.FULL_ACCESS);
        User user = em.find(User.class, new UserKey("TEST_WORKSPACE", "user4"));
        ACLUserEntry[] aclUserEntries = new ACLUserEntry[1];
        aclUserEntries[0] = new ACLUserEntry(new ACL(), user, ACL.Permission.FULL_ACCESS);
        userManagerBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user4");
        documentManagerBean.createDocumentMaster("user1", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT5", aclUserEntries, aclUserGroupEntries);
        documentManagerBean.testDocumentCheckIn("user1", new DocumentRevisionKey(new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5"), "A"));
        documentManagerBean.testDocumentCheckOut("user4", new DocumentRevisionKey(new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5"), "A"));
    }


}
