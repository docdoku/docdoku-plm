package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IGCMSenderLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESSearcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentManagerBeanTest {

    private static final String WORKSPACE_ID="TestWorkspace";
    private static final String DOCUMENT_ID="TestDocument";
    private static final String DOCUMENT_TEMPLATE_ID="temp_1";
    private static final String FILE_NAME="uplodedFile";
    private static final long DOCUMENT_SIZE = 22;
    private static final String VERSION ="A" ;
    private static final int ITERATION = 1;

    @InjectMocks
    DocumentManagerBean  documentManagerBean = new DocumentManagerBean();

    @Mock
    private EntityManager em;

    @Mock
    private SessionContext ctx;

    @Mock
    private IUserManagerLocal userManager;
    @Mock
    private IMailerLocal mailer;
    @Mock
    private IGCMSenderLocal gcmNotifier;
    @Mock
    private ESIndexer esIndexer;
    @Mock
    private ESSearcher esSearcher;
    @Mock
    private IDataManagerLocal dataManager;

    @Spy
    private Account account;
    @Spy
    private Workspace workspace ;
    @Spy
    private User user;
    @Spy
    DocumentMasterTemplate documentMasterTemplate;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = Mockito.spy(new Account("user2" , "user2", "user2@docdoku.com", "en",new Date()));
        workspace = new Workspace(WORKSPACE_ID,account, "pDescription", false);
        user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");
        documentMasterTemplate= new DocumentMasterTemplate(workspace, DOCUMENT_TEMPLATE_ID, user,"","");

    }

    /**
     * test the upload of file in document template
     * @throws Exception
     */
    @Test
    public void saveFileInTemplate() throws Exception {
        //Given
        DocumentMasterTemplateKey pDocMTemplateKey = Mockito.spy(new DocumentMasterTemplateKey(WORKSPACE_ID,DOCUMENT_ID));

        Mockito.when(userManager.checkWorkspaceWriteAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentMasterTemplate.class, pDocMTemplateKey)).thenReturn(documentMasterTemplate);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInTemplate(pDocMTemplateKey, FILE_NAME, DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(WORKSPACE_ID+"/document-templates/"+DOCUMENT_TEMPLATE_ID+"/"+FILE_NAME));
    }

    /**
     * test the upload of file in document
     * @throws Exception
     */
    @Test
    public void saveFileInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace,DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, ITERATION,user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(WORKSPACE_ID,DOCUMENT_ID, VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(WORKSPACE_ID,DOCUMENT_ID, VERSION,ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(WORKSPACE_ID,DOCUMENT_ID, VERSION))).thenReturn(documentRevision);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInDocument(iterationKey, FILE_NAME, DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(WORKSPACE_ID+"/documents/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+FILE_NAME));

    }
}