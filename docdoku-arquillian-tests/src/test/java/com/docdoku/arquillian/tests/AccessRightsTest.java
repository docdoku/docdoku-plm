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
import com.docdoku.core.common.*;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;
import junit.framework.Assert;
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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING )
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

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests.war")
                .addPackage(Workspace.class.getPackage())
                .addClasses(
                        Account.class,
                        Organization.class,
                        Credential.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        IUserManagerLocal.class,
                        IWorkspaceManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        UserManagerBean.class,
                        Workspace.class,
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
    }


    @Test
    public void Test1_testSimpleCreation() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : testSimpleCreation");
        userManagerBean.testWorkspaceCreation("user1", "TEST_WORKSPACE");
        documentManagerBean.testFolderCreation("user1", "TEST_WORKSPACE", "TEST_FOLDER");
        userManagerBean.testAddingUserInWorkspace("user1", "user2", "TEST_WORKSPACE");
        documentManagerBean.testDocumentCreation("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT0", null, null);

    }

    @Test
    public void Test2_testMatrixRights1() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : testMatrixRights1");
        userManagerBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user2"}, "TEST_WORKSPACE", false);
        documentManagerBean.testDocumentCreation("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT1", null, null);
    }

    @Test
    public void Test3_testMatrixRights2() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : testMatrixRights2");
        userManagerBean.testGroupCreation("user1", "TEST_WORKSPACE", "group1");
        userManagerBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group1"}, "TEST_WORKSPACE", true);
        userManagerBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
        userManagerBean.testAddingUserInWorkspace("user1", "user3", "TEST_WORKSPACE");
        userManagerBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user3"}, "TEST_WORKSPACE", false);
        documentManagerBean.testDocumentCreation("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT3", null, null);
    }

    @Test
    public void Test4_testMatrixRights3() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : testMatrixRights3");
        userManagerBean.testGroupCreation("user1", "TEST_WORKSPACE", "group2");
        userManagerBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group2"}, "TEST_WORKSPACE", false);
        userManagerBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group1"}, "TEST_WORKSPACE", true);
        userManagerBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
        userManagerBean.testAddingUserInGroup("user1", "group2", "TEST_WORKSPACE", "user3");
        documentManagerBean.testDocumentCreation("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT4", null, null);
    }

    @Test
    public void Test5_testMatrixRights4() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : testMatrixRights4");
        userManagerBean.testAddingUserInWorkspace("user1", "user4", "TEST_WORKSPACE");
        userManagerBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user4"}, "TEST_WORKSPACE", true);
        User user = em.find(User.class, new UserKey("TEST_WORKSPACE", "user4"));
        ACLUserEntry[] aclUserEntries = new ACLUserEntry[1];
        aclUserEntries[0] = new ACLUserEntry(new ACL(), user, ACL.Permission.FULL_ACCESS);

        userManagerBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user4");
        UserGroup userGroup = em.find(UserGroup.class, new UserGroupKey("TEST_WORKSPACE", "group1"));
        ACLUserGroupEntry[] aclUserGroupEntries = new ACLUserGroupEntry[1];
        aclUserGroupEntries[0] = new ACLUserGroupEntry(new ACL(), userGroup, ACL.Permission.FULL_ACCESS);

        documentManagerBean.testDocumentCreation("user1", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT5", aclUserEntries, aclUserGroupEntries);
    }

    @Test
    public void Test6_testCheckInCheckOut() throws Exception {
        Logger.getLogger(AccessRightsTest.class.getName()).log(Level.INFO, "Test method : testCheckInCheckOut");
        documentManagerBean.testDocumentCheckIn("user1", new DocumentRevisionKey(new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5"), "A"));
        documentManagerBean.testDocumentCheckOut("user4", new DocumentRevisionKey(new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5"), "A"));
    }


}
