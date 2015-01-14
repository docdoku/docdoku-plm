package com.docdoku.test.arquillian;

import com.docdoku.core.change.ChangeIssue;
import com.docdoku.core.change.ChangeItem;
import com.docdoku.core.change.ChangeOrder;
import com.docdoku.core.change.ChangeRequest;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.AccessRightException;
import com.docdoku.core.exceptions.UserNotActiveException;
import com.docdoku.core.exceptions.UserNotFoundException;
import com.docdoku.core.exceptions.WorkspaceNotFoundException;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterTemplate;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
import com.docdoku.server.*;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESMapper;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.esindexer.ESTools;
import com.docdoku.server.gcm.GCMSenderBean;
import com.docdoku.test.arquillian.services.TestChangeManagerBean;
import com.docdoku.test.arquillian.services.TestDocumentManagerBean;
import com.docdoku.test.arquillian.services.TestPartManagerBean;
import com.docdoku.test.arquillian.services.TestUserManagerBean;
import com.docdoku.test.arquillian.util.TestUtil;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
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

import static org.junit.Assert.assertTrue;

/**
 * @author Asmae CHADID
 */
@RunWith(Arquillian.class)
public class ChangeTest {

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @EJB
    private TestPartManagerBean partManagerBean;

    @EJB
    private TestChangeManagerBean changeManagerBean;


    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;


    private static final int COUNT = 5;


    private TestUtil util =new TestUtil();

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests-change.war")
                .addPackage(Workspace.class.getPackage())
                .addClasses(
                        Account.class,
                        ACLUserEntry.class,
                        ACLUserGroupEntry.class,
                        Organization.class,
                        Credential.class,
                        ChangeOrder.class,
                        ChangeRequest.class,
                        ChangeIssue.class,
                        ChangeManagerBean.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
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
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        InstanceAttributeTemplate.class,
                        IUserManagerLocal.class,
                        IWorkspaceManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        PartMaster.class,
                        PartMasterTemplate.class,
                        PartRevision.class,
                        PartRevisionKey.class,
                        ProductManagerBean.class,
                        ChangeManagerBean.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        TestChangeManagerBean.class,
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
        em.clear();
        em.joinTransaction();
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
    public void createChangeRequestSimple() throws UserNotFoundException, UserNotActiveException, WorkspaceNotFoundException, AccessRightException {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : createChangeRequest");
        ChangeRequest changeRequest = changeManagerBean.createRequest(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "newRequest", "description", 0, ChangeItem.Priority.MEDIUM, "", ChangeItem.Category.ADAPTIVE);
        Assert.assertTrue(changeManagerBean.getAllChangeRequest(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST).size() == 1);


    }

    @Test
    public void createChangeIssueSimple() throws UserNotFoundException, AccessRightException, WorkspaceNotFoundException, UserNotActiveException {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : createChangeRequest");
        ChangeIssue changeIssue = changeManagerBean.createIssue(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "newRequest", "description", "0", ChangeItem.Priority.MEDIUM, "", ChangeItem.Category.ADAPTIVE);
        Assert.assertTrue(changeManagerBean.getAllChangeRequest(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST).size() == 1);

    }

    @Test
    public void createChangeOrderSimple() throws Exception {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : createChangeRequest");
        userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
        ChangeOrder changeOrder = changeManagerBean.createOrder(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "newRequest", "description",0, ChangeItem.Priority.MEDIUM, "", ChangeItem.Category.ADAPTIVE);
        assertTrue(changeManagerBean.getAllChangeOrder(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST).size() == 1);
    }



}
