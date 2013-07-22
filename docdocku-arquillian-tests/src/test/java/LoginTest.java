import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.Credential;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.security.WorkspaceUserMembership;
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
public class LoginTest {


    @EJB
    private TestEJBBean testBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;

    static final int COUNT = 3;

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test3.war")
                .addPackage(Account.class.getPackage())
                .addPackage(Workspace.class.getPackage())
                .addPackage(Credential.class.getPackage())
                .addPackage(UserGroupMapping.class.getPackage())
                .addClasses(
                        Account.class,
                        Workspace.class,
                        TestEJBBean.class,
                        ITestBean.class,
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
        //  System.setProperty("java.security.auth.login.config", "/Users/asmaechadid/Developer/PLM/docdoku-plm/docdocku-arquillian-tests/src/test/java/login.conf");
        clearData();
        insertData();
        startTransaction();
    }

    private void clearData() throws Exception {
        utx.begin();
        em.joinTransaction();
        System.out.println("Dumping old records...");


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

        System.out.println("Inserting records...");

        for (int i = 1; i <= COUNT; i++) {
            Account account = new Account("user" + i, "user" + i, "user" + i + "@docdoku.com", "FR", new Date());
            Workspace workspace = new Workspace("w" + i, account, "", Workspace.VaultType.DEMO, false);
            User user = new User(workspace, "user" + i, "user" + i, "user" + i + "@docdoku.com", "en");
            em.persist(Credential.createCredential(user.getLogin(), "password"));
            em.persist(new UserGroupMapping(user.getLogin()));
            em.persist(account);
            em.persist(workspace);
            em.persist(user);
        }

        Workspace workspace = new Workspace();
        workspace.setId("w5");
        User user = new User(workspace, "user1");
        em.persist(workspace);
        em.persist(user);
        utx.commit();
        // clear the persistence context (first-level cache)
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

    //@Test
    public void workspaceAccess() {
        try {
            testBean.workspaceReadOnlyAccess();
        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
        }
    }

    //  @Test
    public void groupAndUserAccess() {
        try {
            testBean.UserAndGroupAccess(true, false);
        } catch (Exception e) {
            if (e instanceof AccessRightException)
                Assert.assertTrue(true);
        }
    }


    public void UserInSeveralGroups() {
        try {
            testBean.UserInSeveralGroups(null, null);
        } catch (Exception e) {

            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
            else if (e instanceof UserNotActiveException)
                Assert.assertFalse(true);
        }
    }
    @Test
    public void UserAndACLOnSpecificEntity(){
        try {
            testBean.UserAndACLOnSpecificEntity(true, ACL.Permission.FULL_ACCESS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
            else if (e instanceof UserNotActiveException)
                Assert.assertFalse(true);
        }
    }

    //@Test
    public void UserGrpAndACLOnSpecificEntity(){
        try {
            testBean.UserGrpAndACLOnSpecificEntity(true, ACL.Permission.FULL_ACCESS);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if (e instanceof AccessRightException)
                Assert.assertFalse(true);
            else if (e instanceof UserNotActiveException)
                Assert.assertFalse(true);
        }
    }


    public void findAllPersistedObjectUsingJpqlQuery() {

        String fetchingAllAccountsInJpql = "select a from Account  a ";
        String fetchingAllUsersInJpql = "select u from User  u ";
        String fetchingAllCredentialsInJpql = "select c from Credential c ";
        String fetchingAllWorkspaceInJpql = "select w from Workspace w ";

        System.out.println("Selecting (using JPQL)...");
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
            System.out.println("Account: " + account);
            retrievedAccountLogins.add(account.getLogin());
        }
    }

    private static void assertContainsAllWorkspaces(Collection<Workspace> retrievedWorkspaces) {
        Assert.assertEquals(COUNT, +1, retrievedWorkspaces.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Workspace workspace : retrievedWorkspaces) {
            System.out.println("Workspace : " + workspace);
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
