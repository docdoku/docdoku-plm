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
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.*;

/**
 * @author: Asmae CHADID
 */

@RunWith(Arquillian.class)
public class TestLauncher {

    @EJB
    private TestEJBBean testBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    static final int COUNT = 5;

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests.war")
                .addPackage(Account.class.getPackage())
                .addPackage(Workspace.class.getPackage())
                .addPackage(Credential.class.getPackage())
                .addPackage(UserGroupMapping.class.getPackage())
                .addClasses(
                        Account.class,
                        Workspace.class,
                        TestEJBBean.class,
                        IUserManagerLocal.class,
                        UserManagerBean.class,
                        IMailerLocal.class,
                        MailerBean.class,
                        IndexerBean.class,
                        IndexSearcherBean.class,
                        IDocumentManagerLocal.class,
                        DocumentManagerBean.class,
                        DataManagerBean.class,
                        IDataManagerLocal.class,
                        WorkspaceUserMembership.class,
                        Credential.class,
                        IWorkspaceManagerLocal.class,
                        WorkspaceManagerBean.class

                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("WEB-INF/sun-web.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }


    @Before
    public void preparePersistenceTest() throws Exception {
        clearData();
        insertData();
        startTransaction();
    }

    private void clearData() throws Exception {
        utx.begin();
        em.joinTransaction();

        em.createQuery("delete from Credential ").executeUpdate();
        em.createQuery("delete from UserGroupMapping ").executeUpdate();
        em.createQuery("delete from Workspace ").executeUpdate();
        em.createQuery("delete from User ").executeUpdate();
        em.createQuery("delete from Account").executeUpdate();
        utx.commit();
    }

    private void insertData() throws Exception {
        utx.begin();
        em.joinTransaction();
        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date());
            Workspace workspace = new Workspace("workspace" + i, account, "", Workspace.VaultType.DEMO, false);
            User user = new User(workspace, "user" + i, "user" + i, "user" + i + "@docdoku.com", "en");
            em.persist(Credential.createCredential(user.getLogin(), "password"));
            em.persist(new UserGroupMapping(user.getLogin()));
            em.persist(account);
            em.persist(workspace);
            em.persist(user);
        }
        utx.commit();
        em.clear();
    }

    private void startTransaction() throws Exception {
        utx.begin();
        em.joinTransaction();
    }

    @After
    public void commitTransaction() throws Exception {
        utx.commit();
    }

    @Test
    public void launchTests() {

        String[] users = new String[1];
        String[] groups = new String[1];
        users[0] = "user2";
        groups[0] = "group1";

        try {
            testBean.testWorkspaceCreation("user1", "TEST_WORKSPACE");
            testBean.testFolderCreation("user1", "TEST_WORKSPACE", "TEST_FOLDER");
            testBean.testAddingUserInWorkspace("user1", "user2", "TEST_WORKSPACE");
            testBean.testDocumentCreation("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT0", null, null);
            testBean.testGrantingUserAccessInWorkspace("user1", users, "TEST_WORKSPACE", false);
        } catch (Exception e) {
            Assert.assertFalse(true);
        }

        try {
            testBean.testDocumentCreation("user2", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT1", null, null);
        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
        }

        try {
            testBean.testGroupCreation("user1", "TEST_WORKSPACE", "group1");
            testBean.testGrantingUserGroupAccessInWorkspace("user1", groups, "TEST_WORKSPACE", true);
            testBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
            testBean.testAddingUserInWorkspace("user1", "user3", "TEST_WORKSPACE");
            users[0] = "user3";
            testBean.testGrantingUserAccessInWorkspace("user1", users, "TEST_WORKSPACE", false);
            testBean.testDocumentCreation("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT3", null, null);
        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
        }

        try {
            testBean.testGroupCreation("user1", "TEST_WORKSPACE", "group2");
            groups[0] = "group2";
            testBean.testGrantingUserGroupAccessInWorkspace("user1", groups, "TEST_WORKSPACE", false);

            groups[0] = "group1";
            testBean.testGrantingUserGroupAccessInWorkspace("user1", groups, "TEST_WORKSPACE", true);

            testBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user3");
            testBean.testAddingUserInGroup("user1", "group2", "TEST_WORKSPACE", "user3");

            testBean.testDocumentCreation("user3", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT4", null, null);

        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
        }

        try {
            testBean.testAddingUserInWorkspace("user1", "user4", "TEST_WORKSPACE");
            users[0] = "user4";
            testBean.testGrantingUserAccessInWorkspace("user1", users, "TEST_WORKSPACE", true);
            User user = em.find(User.class, new UserKey("TEST_WORKSPACE", "user4"));
            ACLUserEntry[] aclUserEntries = new ACLUserEntry[1];
            aclUserEntries[0] = new ACLUserEntry(new ACL(), user, ACL.Permission.FULL_ACCESS);

            testBean.testAddingUserInGroup("user1", "group1", "TEST_WORKSPACE", "user4");
            UserGroup userGroup = em.find(UserGroup.class, new UserGroupKey("TEST_WORKSPACE", "group1"));
            ACLUserGroupEntry[] aclUserGroupEntries = new ACLUserGroupEntry[1];
            aclUserGroupEntries[0] = new ACLUserGroupEntry(new ACL(), userGroup, ACL.Permission.FULL_ACCESS);

            testBean.testDocumentCreation("user1", "TEST_WORKSPACE/TEST_FOLDER", "DOCUMENT5", aclUserEntries, aclUserGroupEntries);

        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
        }

        try {
            testBean.testDocumentCheckIn("user1", new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5", "A"));
            testBean.testDocumentCheckOut("user4", new DocumentMasterKey("TEST_WORKSPACE", "DOCUMENT5", "A"));
        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
        }
    }

    public void findAllPersistedObjectUsingJpqlQuery() {

        String fetchingAllAccountsInJpql = "select a from Account  a ";
        String fetchingAllUsersInJpql = "select u from User  u ";
        String fetchingAllCredentialsInJpql = "select c from Credential c ";
        String fetchingAllWorkspaceInJpql = "select w from Workspace w ";


        List<Account> accounts = em.createQuery(fetchingAllAccountsInJpql, Account.class).getResultList();
        List<User> users = em.createQuery(fetchingAllUsersInJpql, User.class).getResultList();
        List<Credential> credentials = em.createQuery(fetchingAllCredentialsInJpql, Credential.class).getResultList();
        List<Workspace> workspaces = em.createQuery(fetchingAllWorkspaceInJpql, Workspace.class).getResultList();

        assertContainsAllAccounts(accounts);
        assertContainsAllWorkspaces(workspaces);
        assertContainsAllCredentials(credentials);
        assertContainsAllUsers(users);
    }

    private static void assertContainsAllAccounts(Collection<Account> retrievedAccounts) {
        Assert.assertEquals(COUNT, retrievedAccounts.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Account account : retrievedAccounts) {
            retrievedAccountLogins.add(account.getLogin());
        }
    }

    private static void assertContainsAllWorkspaces(Collection<Workspace> retrievedWorkspaces) {
        Assert.assertEquals(COUNT, retrievedWorkspaces.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Workspace workspace : retrievedWorkspaces) {
            retrievedAccountLogins.add(workspace.getId());
        }
    }

    private static void assertContainsAllCredentials(Collection<Credential> retrievedCredentials) {
        Assert.assertEquals(COUNT, retrievedCredentials.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Credential credential : retrievedCredentials) {
            retrievedAccountLogins.add(credential.toString());
        }
    }

    private static void assertContainsAllUsers(Collection<User> retrievedUsers) {
        Assert.assertEquals(COUNT, retrievedUsers.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (User user : retrievedUsers) {
            retrievedAccountLogins.add(user.getLogin());
        }
        Assert.assertTrue(retrievedAccountLogins.contains("user1"));

    }
}
