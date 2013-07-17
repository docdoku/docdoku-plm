/**
 * @author: Asmae CHADID
 */
import com.docdoku.core.common.Account;
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
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.util.*;

@RunWith(Arquillian.class)
public class AccountPersistenceTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test2.war")
                .addPackage(Account.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static final String[] ACCOUNT_NAMES = {
            "user1",
            "user2",
            "user3",
            "user4"
    };

    @PersistenceContext
    EntityManager em;

    @Inject
    UserTransaction utx;

    // tests go here

    @Before
    public void preparePersistenceTest() throws Exception {
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
        for (String name : ACCOUNT_NAMES) {
            Account account = new Account(name,name,name+"@docdoku.com","FR",new Date());
            em.persist(account);
        }
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

        // when
        System.out.println("Selecting (using JPQL)...");
        List<Account> accounts = em.createQuery(fetchingAllAccountsInJpql, Account.class).getResultList();

        // then
        System.out.println("Found " + accounts.size() + " accounts (using JPQL):");
        assertContainsAllAccounts(accounts);
    }

    private static void assertContainsAllAccounts(Collection<Account> retrievedAccounts) {
        Assert.assertEquals(ACCOUNT_NAMES.length, retrievedAccounts.size());
        final Set<String> retrievedAccountLogins = new HashSet<String>();
        for (Account account : retrievedAccounts) {
            System.out.println("* " + account);
            retrievedAccountLogins.add(account.getLogin());
        }
        Assert.assertTrue(retrievedAccountLogins.containsAll(Arrays.asList(ACCOUNT_NAMES)));
    }




}

