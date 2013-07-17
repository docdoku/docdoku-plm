import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.security.Credential;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.security.WorkspaceUserMembership;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
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
    ITestBean helloEJBBean;



    @PersistenceContext
    EntityManager em;

    @Inject
    UserTransaction utx;




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
                        WorkspaceUserMembership.class,
                        Credential.class
                )
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

    }


    public void whoAMI() throws Exception {


        // System.out.println(documentManagerLocal.whoAmI("workspace1").getLogin());
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
        em.createQuery("delete from Account").executeUpdate();
        utx.commit();
    }

    private void insertData() throws Exception {
        utx.begin();
        em.joinTransaction();
        System.out.println("Inserting records...");
        Account admin = new Account("user1","user1","user1"+"@docdoku.com","FR",new Date());
        Workspace workspace = new Workspace("w1",admin,"", Workspace.VaultType.DEMO,false);
         User user = new User(workspace,"user1");
         user.setEmail("user1@gmail.com");
         user.setLanguage("fr");
         user.setName("user1 name");
         User user2 = new User(workspace,"user2");
        user2.setEmail("user2@gmail.com");
        user2.setLanguage("fr");
        user2.setName("user2");
        UserGroupMapping userGroupMapping= new UserGroupMapping("user1","users");
       // Credential credential = new Credential();
       // Credential.createCredential("user1", "password");

        workspace.setId("w1");
        em.persist(admin);
        em.persist(workspace);
        em.persist(user);
        em.persist(user2);
        em.persist(userGroupMapping);
        em.persist( Credential.createCredential("user1", "password"));
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

    @Test
    public void shouldFindAllAccountsUsingJpqlQuery() throws Exception {
        // given
        String fetchingAllAccountsInJpql = "select a from Account  a ";
        String fetchingAllUsersInJpql = "select u from User  u ";
        String fetchingAllCredentialsInJpql = "select c from Credential c ";

        // when
        System.out.println("Selecting (using JPQL)...");
        List<Account> accounts = em.createQuery(fetchingAllAccountsInJpql, Account.class).getResultList();
        List<User> users = em.createQuery(fetchingAllUsersInJpql, User.class).getResultList();
        List<Credential> credentials = em.createQuery(fetchingAllCredentialsInJpql, Credential.class).getResultList();

        // then
        System.out.println("Found " + accounts.size() + " accounts (using JPQL):");
        assertContainsAllAccounts(accounts);
        assertContainsAllCredentials(credentials);
        helloEJBBean.testEJB("");
    }

    private static void assertContainsAllAccounts(Collection<Account> retrievedAccounts) {
        Assert.assertEquals(1, retrievedAccounts.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Account account : retrievedAccounts) {
            System.out.println("* " + account);
            retrievedAccountLogins.add(account.getLogin());
        }
    }

    private static void assertContainsAllCredentials(Collection<Credential> retrievedCredentials) {
        Assert.assertEquals(1, retrievedCredentials.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Credential credential : retrievedCredentials) {
            System.out.println("Credential login " + credential.getLogin());
            System.out.println("Credential password " + credential.getPassword());
            retrievedAccountLogins.add(credential.toString());
        }
    }
    private static void assertContainsAllUsers(Collection<User> retrievedUsers) {
        Assert.assertEquals(2, retrievedUsers.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (User user : retrievedUsers) {
            System.out.println("* " + user);
            retrievedAccountLogins.add(user.getLogin());
        }
      //  Assert.assertTrue(retrievedAccountLogins.contains("user1"));

    }
}
