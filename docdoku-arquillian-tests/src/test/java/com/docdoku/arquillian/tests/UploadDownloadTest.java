package com.docdoku.arquillian.tests;

import com.docdoku.arquillian.tests.services.TestDocumentManagerBean;
import com.docdoku.arquillian.tests.services.TestPartManagerBean;
import com.docdoku.arquillian.tests.services.TestUploadDownloadManagerBean;
import com.docdoku.arquillian.tests.services.TestUserManagerBean;
import com.docdoku.arquillian.tests.util.TestUtil;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.Organization;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.DocumentIterationKey;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.gcm.GCMAccount;
import com.docdoku.core.meta.InstanceAttributeTemplate;
import com.docdoku.core.product.*;
import com.docdoku.core.security.*;
import com.docdoku.core.services.*;
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

import javax.activation.DataHandler;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;


/**
 * @author Asmae CHADID
 */

@RunWith(Arquillian.class)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class UploadDownloadTest {

    @EJB
    private ESIndexer esIndexer;

    @EJB
    private TestDocumentManagerBean documentManagerBean;
    @EJB
    private TestUserManagerBean userManagerBean;

    @EJB
    private TestPartManagerBean partManagerBean;

    @EJB
    private TestUploadDownloadManagerBean uploadDownloadManagerBean;

    @PersistenceContext
    private EntityManager em;

    @Inject
    private UserTransaction utx;


    private static final int COUNT = 5;

    @Deployment
    public static Archive<?> createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "docdoku-arquillian-tests.war")
                .addPackage(DocumentManagerBean.class.getPackage())
                .addClasses(
                        Account.class,
                        ACLUserEntry.class,
                        ACLUserGroupEntry.class,
                        Organization.class,
                        Credential.class,
                        DataManagerBean.class,
                        DocumentManagerBean.class,
                        DocumentIterationKey.class,
                        ESIndexer.class,
                        ESMapper.class,
                        ESSearcher.class,
                        ESTools.class,
                        GCMAccount.class,
                        GCMSenderBean.class,
                        IDataManagerLocal.class,
                        IDocumentManagerLocal.class,
                        IProductManagerLocal.class,
                        IGCMSenderLocal.class,
                        IMailerLocal.class,
                        InstanceAttributeTemplate.class,
                        IUserManagerLocal.class,
                        IUploadDownloadWS.class,
                        IWorkspaceManagerLocal.class,
                        JsonValue.class,
                        MailerBean.class,
                        PartMaster.class,
                        PartMasterTemplate.class,
                        PartRevision.class,
                        PartRevisionKey.class,
                        ProductManagerBean.class,
                        PartUsageLink.class,
                        TestDocumentManagerBean.class,
                        TestUserManagerBean.class,
                        TestUploadDownloadManagerBean.class,
                        TestPartManagerBean.class,
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

        userManagerBean.testWorkspaceCreation(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST);
        documentManagerBean.createFolder(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST,TestUtil.FOLDER_TEST);

    }

    @Test
    public void test1_uploadToDownloadFromPart() throws Exception {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : uploadToDownloadFromPart");
        partManagerBean.createPartMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", " ", true, null, "", null, new HashMap<String, String>(), new ACLUserEntry[0], new ACLUserGroupEntry[0]);

        PartMaster partMaster = partManagerBean.findPartMasterById(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125");
        assertTrue(partMaster != null);
        URL binURL = getClass().getResource("/com/docdoku/arquillian/tests/bike.bin");
        DataHandler data = new DataHandler(binURL);
        uploadDownloadManagerBean.uploadToPart(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST,"plane_125","A",1,"bike.bin",data);
        DataHandler dataHandler= uploadDownloadManagerBean.downloadFromPart(TestUtil.USER1_TEST,TestUtil.WORKSPACE_TEST,"plane_125","A",1,"bike.bin");
        Assert.assertTrue(dataHandler.getDataSource().getName().equals("bike.bin"));
        Assert.assertTrue(dataHandler.getDataSource().getContentType().equals(data.getDataSource().getContentType()));
        Assert.assertTrue(dataHandler.getDataSource().getInputStream().available() == data.getDataSource().getInputStream().available());

    }

    @Test
    public void test2_uploadToDownloadFromDocument() throws Exception {
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : uploadToDownloadFromDocument");
        DocumentRevision documentRevision= documentManagerBean.createDocumentMaster(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST + "/"+TestUtil.FOLDER_TEST, "plane_125", new ACLUserEntry[0], new ACLUserGroupEntry[0]);
        assertTrue(documentRevision != null);
        URL binURL = getClass().getResource("/com/docdoku/arquillian/tests/bike.bin");
        DataHandler data = new DataHandler(binURL);
        uploadDownloadManagerBean.uploadToDocument(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", "A", 1, "bike.bin", data);
        DataHandler dataHandler= uploadDownloadManagerBean.downloadFromDocument(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "plane_125", "A", 1, "bike.bin");
        Assert.assertTrue(dataHandler.getDataSource().getName().equals("bike.bin"));
        Assert.assertTrue(dataHandler.getDataSource().getContentType().equals(data.getDataSource().getContentType()));
        Assert.assertTrue(dataHandler.getDataSource().getInputStream().available() == data.getDataSource().getInputStream().available());

    }

    @Test
    public void test3_uploadToTemplate() throws Exception{
        Logger.getLogger(PartTests.class.getName()).log(Level.INFO, "Test method : uploadToTemplate");
        PartMasterTemplate masterTemplate= partManagerBean.createPartMasterTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "planes", "plane_###", new InstanceAttributeTemplate[0], true, true);
        DocumentMasterTemplate  documentMasterTemplate = documentManagerBean.createDocumentTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "", "", new InstanceAttributeTemplate[0], false, false);
        URL binURL = getClass().getResource("/com/docdoku/arquillian/tests/bike.bin");
        DataHandler data = new DataHandler(binURL);
        uploadDownloadManagerBean.uploadToTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST+"/templates", "template1", "bike.bin", data);
        DataHandler dataHandler= uploadDownloadManagerBean.downloadFromTemplate(TestUtil.USER1_TEST, TestUtil.WORKSPACE_TEST, "template1", "bike.bin");
        assertTrue(documentMasterTemplate != null);
        Assert.assertTrue(dataHandler.getDataSource().getName().equals("bike.bin"));
        Assert.assertTrue(dataHandler.getDataSource().getContentType().equals(data.getDataSource().getContentType()));
        Assert.assertTrue(dataHandler.getDataSource().getInputStream().available() == data.getDataSource().getInputStream().available());
    }





}
