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

package com.docdoku.arquillian.tests;

import com.docdoku.core.common.*;
import com.docdoku.core.document.DocumentMasterKey;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
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
import java.util.Date;

/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
@FixMethodOrder(value = MethodSorters.JVM )
public class AccessRightsTest {

    @EJB
    private TestEJBBean testBean;

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
                        Credential.class,
                        GCMAccount.class,
                        Workspace.class,
                        TestEJBBean.class,
                        IUserManagerLocal.class,
                        UserManagerBean.class,
                        IMailerLocal.class,
                        MailerBean.class,
                        IGCMSenderLocal.class,
                        GCMSenderBean.class,
                        IDocumentManagerLocal.class,
                        DocumentManagerBean.class,
                        DataManagerBean.class,
                        IDataManagerLocal.class,
                        WorkspaceUserMembership.class,
                        IWorkspaceManagerLocal.class,
                        WorkspaceManagerBean.class,
                        JsonValue.class,
                        ESIndexer.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("WEB-INF/sun-web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }


    //@After
    public void clearData() throws Exception {
        utx.begin();
        em.joinTransaction();
        em.createQuery("delete from UserGroupMapping").executeUpdate();
        em.createQuery("delete from Account").executeUpdate();
        em.createQuery("delete from Credential").executeUpdate();
        utx.commit();
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
    public void atestSimpleCreation() throws Exception {
        testBean.testWorkspaceCreation("user1", "TEST_WORKSPACE");
        testBean.testFolderCreation("user1", "TEST_WORKSPACE", "TEST_FOLDER");
        testBean.testAddingUserInWorkspace("user1", "user2", "TEST_WORKSPACE");
        testBean.testDocumentCreation("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT0", null, null);

    }

    @Test
    public void testMatrixRights1() throws Exception {
        testBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user2"}, "TEST_WORKSPACE", false);
        testBean.testDocumentCreation("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT1", null, null);
    }

    @Test
    public void testMatrixRights2() throws Exception {
        testBean.testGroupCreation("user1", "TEST_WORKSPACE", "group1");
        testBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group1"}, "TEST_WORKSPACE", true);
        testBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
        testBean.testAddingUserInWorkspace("user1", "user3", "TEST_WORKSPACE");
        testBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user3"}, "TEST_WORKSPACE", false);
        testBean.testDocumentCreation("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT3", null, null);
    }

    @Test
    public void testMatrixRights3() throws Exception {
        testBean.testGroupCreation("user1", "TEST_WORKSPACE", "group2");
        testBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group2"}, "TEST_WORKSPACE", false);
        testBean.testGrantingUserGroupAccessInWorkspace("user1", new String[]{"group1"}, "TEST_WORKSPACE", true);
        testBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
        testBean.testAddingUserInGroup("user1", "group2", "TEST_WORKSPACE", "user3");
        testBean.testDocumentCreation("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT4", null, null);
    }

    @Test
    public void testMatrixRights4() throws Exception {
        testBean.testAddingUserInWorkspace("user1", "user4", "TEST_WORKSPACE");
        testBean.testGrantingUserAccessInWorkspace("user1", new String[]{"user4"}, "TEST_WORKSPACE", true);
        User user = em.find(User.class, new UserKey("TEST_WORKSPACE", "user4"));
        ACLUserEntry[] aclUserEntries = new ACLUserEntry[1];
        aclUserEntries[0] = new ACLUserEntry(new ACL(), user, ACL.Permission.FULL_ACCESS);

        testBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user4");
        UserGroup userGroup = em.find(UserGroup.class, new UserGroupKey("TEST_WORKSPACE", "group1"));
        ACLUserGroupEntry[] aclUserGroupEntries = new ACLUserGroupEntry[1];
        aclUserGroupEntries[0] = new ACLUserGroupEntry(new ACL(), userGroup, ACL.Permission.FULL_ACCESS);

        testBean.testDocumentCreation("user1", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT5", aclUserEntries, aclUserGroupEntries);
    }

    @Test
    public void testCheckInCheckOut() throws Exception {
        testBean.testDocumentCheckIn("user1", new DocumentRevisionKey(new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5"), "A"));
        testBean.testDocumentCheckOut("user4", new DocumentRevisionKey(new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5"), "A"));
    }

}
