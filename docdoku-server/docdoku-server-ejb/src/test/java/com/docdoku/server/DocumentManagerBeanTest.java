/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.document.*;
import com.docdoku.core.exceptions.NotAllowedException;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
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

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Date;

import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentManagerBeanTest {

    private static final String WORKSPACE_ID="TestWorkspace";
    private static final String DOCUMENT_ID ="TestDocument";
    private static final String DOCUMENT_TEMPLATE_ID="temp_1";
    private static final String FILE1_NAME ="uplodedFile";
    private static final String FILE2_NAME="file_à-tèsté.txt";
    private static final String FILE3_NAME="file_à-t*st?! .txt";
    private static final long DOCUMENT_SIZE = 22;
    private static final String VERSION ="A" ;
    private static final int ITERATION = 1;
    private static final String FULL_NAME = WORKSPACE_ID+"/documents/"+DOCUMENT_ID+"/"+VERSION+"/"+ITERATION+"/"+ FILE1_NAME;
    private static final String FOLDER = "newFolder";



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
    @Spy
    BinaryResource binaryResource;
    @Spy
    DocumentIteration documentIteration;

    @Mock
    TypedQuery<DocumentIteration> documentIterationQuery;
    @Spy
    DocumentRevision documentRevision;
    @Spy
    ACL acl;
    @Spy
    Folder folder;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = Mockito.spy(new Account("user2" , "user2", "user2@docdoku.com", "en",new Date(),null));
        workspace = new Workspace(WORKSPACE_ID,account, "pDescription", false);
        user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");
        documentMasterTemplate= new DocumentMasterTemplate(workspace, DOCUMENT_TEMPLATE_ID, user,"","");
        binaryResource = Mockito.spy(new BinaryResource(FULL_NAME,DOCUMENT_SIZE,new Date()));
        documentIteration = Mockito.spy(new DocumentIteration());
        acl.addEntry(user, ACL.Permission.READ_ONLY);
        documentRevision.setACL(acl);
        folder = Mockito.spy(new Folder(WORKSPACE_ID+"/"+user.getName()+"/folders/"+FOLDER));

        documentRevision.setLocation(folder);
        documentIteration.setDocumentRevision(documentRevision);
    }

    /**
     * test the upload of file in document template's
     * @throws Exception
     */
    @Test
    public void saveFileInTemplate() throws Exception {
        //Given
        DocumentMasterTemplateKey pDocMTemplateKey = Mockito.spy(new DocumentMasterTemplateKey(WORKSPACE_ID, DOCUMENT_ID));

        Mockito.when(userManager.checkWorkspaceWriteAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentMasterTemplate.class, pDocMTemplateKey)).thenReturn(documentMasterTemplate);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInTemplate(pDocMTemplateKey, FILE1_NAME, DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(WORKSPACE_ID+"/document-templates/"+DOCUMENT_TEMPLATE_ID+"/"+ FILE1_NAME));
    }

    /**
     * test the upload of file in documents
     * @throws Exception
     */
    @Test
    public void saveFileInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace, DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, ITERATION,user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(WORKSPACE_ID, DOCUMENT_ID, VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(WORKSPACE_ID, DOCUMENT_ID, VERSION,ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(WORKSPACE_ID, DOCUMENT_ID, VERSION))).thenReturn(documentRevision);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInDocument(iterationKey, FILE1_NAME, DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(WORKSPACE_ID+"/documents/"+ DOCUMENT_ID +"/"+VERSION+"/"+ITERATION+"/"+ FILE1_NAME));

    }

    /**
     * test the upload of file  with special characters in documents
     * @throws Exception
     */
    @Test
    public void saveFileWithSpecialCharactersInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace, DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, ITERATION,user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(WORKSPACE_ID, DOCUMENT_ID, VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(WORKSPACE_ID, DOCUMENT_ID, VERSION,ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(WORKSPACE_ID, DOCUMENT_ID, VERSION))).thenReturn(documentRevision);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInDocument(iterationKey, FILE2_NAME, DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(WORKSPACE_ID+"/documents/"+ DOCUMENT_ID +"/"+VERSION+"/"+ITERATION+"/"+ FILE2_NAME));

    }

    /**
     * test the upload of file  with forbidden characters in documents
     * @throws Exception
     */
    @Test(expected = NotAllowedException.class)
    public void saveFileWithForbiddenCharactersInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace, DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, ITERATION,user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(WORKSPACE_ID, DOCUMENT_ID, VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(WORKSPACE_ID, DOCUMENT_ID, VERSION,ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(WORKSPACE_ID, DOCUMENT_ID, VERSION))).thenReturn(documentRevision);
        //When
        documentManagerBean.saveFileInDocument(iterationKey, FILE3_NAME, DOCUMENT_SIZE);

    }

    /**
     *
     *  Test to download a document file as a guest
     */
    @Test
    public void getBinaryResourceAsGuest()throws Exception{
        //Given
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)).thenReturn(true);
        Mockito.when(userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(FULL_NAME))).thenReturn(user);
        Mockito.when(em.find(BinaryResource.class, FULL_NAME)).thenReturn(binaryResource);
        BinaryResource binaryResource = documentManagerBean.getBinaryResource(FULL_NAME);
    }
    /**
     *
     *  Test to download a document file as not a guest
     */
    @Test
    public void getBinaryResourceAsNotGuest()throws Exception{
        //Given
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)).thenReturn(true);
        Mockito.when(userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(FULL_NAME))).thenReturn(user);
        Mockito.when(em.find(BinaryResource.class, FULL_NAME)).thenReturn(binaryResource);
        Mockito.when(documentIterationQuery.getSingleResult()).thenReturn(documentIteration);
        Mockito.when(documentIterationQuery.setParameter("binaryResource", binaryResource)).thenReturn(documentIterationQuery);
        Mockito.when(em.createQuery("SELECT d FROM DocumentIteration d WHERE :binaryResource MEMBER OF d.attachedFiles", DocumentIteration.class)).thenReturn(documentIterationQuery);
        BinaryResource binaryResource = documentManagerBean.getBinaryResource(FULL_NAME);
        //Then
        Assert.assertNotNull(binaryResource);
        Assert.assertTrue(binaryResource.getContentLength() == DOCUMENT_SIZE);
    }

    /**
     *
     * This test will check if the attributs is well manages if the documents has a template with freeze attributs
     *
     */
    @Test
    public void changeAttributs()throws Exception{

    }
}