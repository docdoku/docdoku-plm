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
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceTextAttribute;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IGCMSenderLocal;
import com.docdoku.core.services.IMailerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.esindexer.ESIndexer;
import com.docdoku.server.esindexer.ESSearcher;
import com.docdoku.server.util.DocumentUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ejb.SessionContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentManagerBeanTest {



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
    @Mock
    private TypedQuery<DocumentIteration> documentIterationQuery;
    @Mock
    private TypedQuery<ACL> aclTypedQuery;

    private Account account;
    private Workspace workspace ;
    private User user;
    private DocumentMasterTemplate documentMasterTemplate;
    private BinaryResource binaryResource;
    private DocumentIteration documentIteration;
    private DocumentRevision documentRevision;
    private ACL acl;
    private Folder folder;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = new Account(DocumentUtil.USER_2_LOGIN, DocumentUtil.USER_2_NAME, DocumentUtil.USER2_MAIL, DocumentUtil.LANGUAGE,new Date(),null);
        workspace = new Workspace(DocumentUtil.WORKSPACE_ID,account, DocumentUtil.WORKSPACE_DESCRIPTION, false);
        user = new User(workspace, DocumentUtil.USER_1_LOGIN, DocumentUtil.USER_1_NAME, DocumentUtil.USER1_MAIL, DocumentUtil.LANGUAGE);
        documentMasterTemplate= new DocumentMasterTemplate(workspace, DocumentUtil.DOCUMENT_TEMPLATE_ID, user,"","");
        binaryResource = new BinaryResource(DocumentUtil.FULL_NAME,DocumentUtil.DOCUMENT_SIZE,new Date());
        documentIteration = new DocumentIteration();
        acl = new ACL();
        acl.addEntry(user, ACL.Permission.READ_ONLY);

        folder = new Folder(DocumentUtil.WORKSPACE_ID+"/"+user.getName()+"/folders/"+DocumentUtil.FOLDER);
        documentRevision = new DocumentRevision();
        documentRevision.setLocation(folder);
        documentIteration.setDocumentRevision(documentRevision);
        documentRevision.setACL(acl);
    }

    /**
     * test the upload of file in document template's
     * @throws Exception
     */
    @Test
    public void saveFileInTemplate() throws Exception {
        //Given
        DocumentMasterTemplateKey pDocMTemplateKey = Mockito.spy(new DocumentMasterTemplateKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID));

        Mockito.when(userManager.checkWorkspaceWriteAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentMasterTemplate.class, pDocMTemplateKey)).thenReturn(documentMasterTemplate);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInTemplate(pDocMTemplateKey, DocumentUtil.FILE1_NAME, DocumentUtil.DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DocumentUtil.DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(DocumentUtil.WORKSPACE_ID+"/document-templates/"+DocumentUtil.DOCUMENT_TEMPLATE_ID+"/"+ DocumentUtil.FILE1_NAME));
    }

    /**
     * test the upload of file in documents
     * @throws Exception
     */
    @Test
    public void saveFileInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace, DocumentUtil.DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, DocumentUtil.VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION,DocumentUtil.ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION))).thenReturn(documentRevision);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInDocument(iterationKey, DocumentUtil.FILE1_NAME, DocumentUtil.DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DocumentUtil.DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(DocumentUtil.WORKSPACE_ID+"/documents/"+ DocumentUtil.DOCUMENT_ID +"/"+DocumentUtil.VERSION+"/"+DocumentUtil.ITERATION+"/"+ DocumentUtil.FILE1_NAME));

    }

    /**
     * test the upload of file  with special characters in documents
     * @throws Exception
     */
    @Test
    public void saveFileWithSpecialCharactersInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace, DocumentUtil.DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, DocumentUtil.VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION,DocumentUtil.ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION))).thenReturn(documentRevision);
        //When
        BinaryResource binaryResource= documentManagerBean.saveFileInDocument(iterationKey, DocumentUtil.FILE2_NAME, DocumentUtil.DOCUMENT_SIZE);
        //Then
        Assert.assertTrue(binaryResource.getLastModified()!= null);
        Assert.assertTrue(binaryResource.getContentLength() == DocumentUtil.DOCUMENT_SIZE);
        Assert.assertTrue(!binaryResource.getFullName().isEmpty());
        Assert.assertTrue(binaryResource.getFullName().equals(DocumentUtil.WORKSPACE_ID+"/documents/"+ DocumentUtil.DOCUMENT_ID +"/"+DocumentUtil.VERSION+"/"+DocumentUtil.ITERATION+"/"+ DocumentUtil.FILE2_NAME));

    }

    /**
     * test the upload of file  with forbidden characters in documents
     * @throws Exception
     */
    @Test(expected = NotAllowedException.class)
    public void saveFileWithForbiddenCharactersInDocument() throws Exception {
        //Given
        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace, DocumentUtil.DOCUMENT_ID,user));
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster, DocumentUtil.VERSION,user));
        ArrayList<DocumentIteration> iterations =new ArrayList<DocumentIteration>();
        iterations.add(new DocumentIteration(documentRevision, user));
        documentRevision.setDocumentIterations(iterations);
        documentRevision.setCheckOutUser(user);
        DocumentRevisionKey documentRevisionKey = Mockito.spy(new DocumentRevisionKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION));
        DocumentIterationKey iterationKey = Mockito.spy(new DocumentIterationKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION,DocumentUtil.ITERATION));

        Mockito.when(userManager.checkWorkspaceWriteAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(DocumentUtil.WORKSPACE_ID)).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, documentRevisionKey)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, new DocumentRevisionKey(DocumentUtil.WORKSPACE_ID, DocumentUtil.DOCUMENT_ID, DocumentUtil.VERSION))).thenReturn(documentRevision);
        //When
        documentManagerBean.saveFileInDocument(iterationKey, DocumentUtil.FILE3_NAME, DocumentUtil.DOCUMENT_SIZE);

    }

    /**
     *
     *  Test to download a document file as a guest
     */
    @Test
    public void getBinaryResourceAsGuest()throws Exception{
        //Given
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.GUEST_PROXY_ROLE_ID)).thenReturn(true);
        Mockito.when(userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(DocumentUtil.FULL_NAME))).thenReturn(user);
        Mockito.when(em.find(BinaryResource.class, DocumentUtil.FULL_NAME)).thenReturn(binaryResource);
        BinaryResource binaryResource = documentManagerBean.getBinaryResource(DocumentUtil.FULL_NAME);
    }
    /**
     *
     *  Test to download a document file as not a guest
     */
    @Test
    public void getBinaryResourceAsNotGuest()throws Exception{
        //Given
        Mockito.when(ctx.isCallerInRole(UserGroupMapping.ADMIN_ROLE_ID)).thenReturn(true);
        Mockito.when(userManager.checkWorkspaceReadAccess(BinaryResource.parseWorkspaceId(DocumentUtil.FULL_NAME))).thenReturn(user);
        Mockito.when(em.find(BinaryResource.class, DocumentUtil.FULL_NAME)).thenReturn(binaryResource);
        Mockito.when(documentIterationQuery.getSingleResult()).thenReturn(documentIteration);
        Mockito.when(documentIterationQuery.setParameter("binaryResource", binaryResource)).thenReturn(documentIterationQuery);
        Mockito.when(em.createQuery("SELECT d FROM DocumentIteration d WHERE :binaryResource MEMBER OF d.attachedFiles", DocumentIteration.class)).thenReturn(documentIterationQuery);
        BinaryResource binaryResource = documentManagerBean.getBinaryResource(DocumentUtil.FULL_NAME);
        //Then
        Assert.assertNotNull(binaryResource);
        Assert.assertTrue(binaryResource.getContentLength() == DocumentUtil.DOCUMENT_SIZE);
    }

    /**
     *
     * This test will check if the attributes is well manages if the documents has a template with freeze attributes
     *
     */
    @Test
    public void changeAttributesWithLockedTemplate()throws Exception{

        DocumentMaster documentMaster = new DocumentMaster(workspace, DocumentUtil.DOCUMENT_ID,user);


        documentRevision = new DocumentRevision(documentMaster, "A", user);
        documentIteration = new DocumentIteration(documentRevision, user);
        documentRevision.setCheckOutUser(user);
        documentRevision.setCheckOutDate(new Date());
        ArrayList<DocumentIteration> iterations = new ArrayList<DocumentIteration>();
        iterations.add(documentIteration);
        documentRevision.setDocumentIterations(iterations);

        DocumentRevisionKey rKey = new DocumentRevisionKey(workspace.getId(), documentMaster.getId(), documentRevision.getVersion());

        //Creation of current attributes of the iteration
        InstanceAttribute attribute = new InstanceTextAttribute("Nom", "Testeur", false);
        List<InstanceAttribute> attributesOfIteration = new ArrayList<>();
        attributesOfIteration.add(attribute);
        documentIteration.setInstanceAttributes(attributesOfIteration);

        documentMaster.setAttributesLocked(true);

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, null)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, rKey)).thenReturn(documentRevision);


        try{
            //Test to remove attribute
            documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{}), new DocumentRevisionKey[]{}, null);
            Assert.assertTrue("updateDocument should have raise an exception because we have removed attributes", false);
        }catch (NotAllowedException notAllowedException){
            try{
                //Test with a swipe of attribute
                documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{new InstanceDateAttribute("Nom", new Date(), false)}), new DocumentRevisionKey[]{}, null);
                Assert.assertTrue("updateDocument should have raise an exception because we have changed the attribute type attributes", false);
            }catch (NotAllowedException notAllowedException2){
                try {
                    //Test without modifying the attribute
                    documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{attribute}), new DocumentRevisionKey[]{}, null);
                    //Test with a new value of the attribute
                    documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{new InstanceTextAttribute("Nom", "Testeur change", false)}), new DocumentRevisionKey[]{}, null);
                } catch (NotAllowedException notAllowedException3){
                    Assert.assertTrue("updateDocument shouldn't have raised an exception because we haven't change the number of attribute or the type", false);
                }
            }
        }
    }

    /**
     *
     * This test will check if the attributes is well manages if the documents has a template with freeze attributes
     *
     */
    @Test
    public void changeAttributesWithUnlockedTemplate()throws Exception{

        DocumentMaster documentMaster = new DocumentMaster(workspace, DocumentUtil.DOCUMENT_ID,user);

        documentRevision = new DocumentRevision(documentMaster, "A", user);
        documentIteration = new DocumentIteration(documentRevision, user);
        documentRevision.setCheckOutUser(user);
        documentRevision.setCheckOutDate(new Date());
        ArrayList<DocumentIteration> iterations = new ArrayList<DocumentIteration>();
        iterations.add(documentIteration);
        documentRevision.setDocumentIterations(iterations);

        DocumentRevisionKey rKey = new DocumentRevisionKey(workspace.getId(), documentMaster.getId(), documentRevision.getVersion());

        //Creation of current attributes of the iteration
        InstanceAttribute attribute = new InstanceTextAttribute("Nom", "Testeur", false);
        List<InstanceAttribute> attributesOfIteration = new ArrayList<>();
        attributesOfIteration.add(attribute);
        documentIteration.setInstanceAttributes(attributesOfIteration);

        documentMaster.setAttributesLocked(false);

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(DocumentRevision.class, null)).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, rKey)).thenReturn(documentRevision);


        try{
            //Test to remove attribute
            documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{}), new DocumentRevisionKey[]{}, null);
            //Add the attribute
            documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{attribute}), new DocumentRevisionKey[]{}, null);
            //Change the value of the attribute
            documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{new InstanceTextAttribute("Nom", "Testeur change", false)}), new DocumentRevisionKey[]{}, null);
            //Change the type of the attribute
            documentManagerBean.updateDocument(documentIteration.getKey(), "test", Arrays.asList(new InstanceAttribute[]{new InstanceDateAttribute("Nom", new Date(), false)}), new DocumentRevisionKey[]{}, null);
        } catch (NotAllowedException notAllowedException3){
            Assert.assertTrue("updateDocument shouldn't have raised an exception because the attribute are not frozen", false);
        }
    }

    /**
     *
     * This test will check if the ACL is null when removing it from a document
     *
     */

    @Test
    public void removeACLFromDocument()throws Exception{

        user = new User(workspace, DocumentUtil.USER_1_LOGIN, DocumentUtil.USER_1_NAME, DocumentUtil.USER1_MAIL, DocumentUtil.LANGUAGE);

        DocumentMaster documentMaster = new DocumentMaster(workspace, DocumentUtil.DOCUMENT_ID,user);
        documentRevision = new DocumentRevision(documentMaster, "A", user);
        documentIteration = new DocumentIteration(documentRevision, user);
        documentRevision.setCheckOutUser(user);
        documentRevision.setCheckOutDate(new Date());
        acl = new ACL();
        acl.addEntry(user, ACL.Permission.READ_ONLY);
        documentRevision.setACL(acl);

        DocumentRevisionKey pKey = new DocumentRevisionKey(workspace.getId(), documentMaster.getId(), documentRevision.getVersion());

        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.getReference(DocumentRevision.class, pKey)).thenReturn(documentRevision);

        Mockito.when(aclTypedQuery.setParameter(Matchers.anyString(),Matchers.any())).thenReturn(aclTypedQuery);
        Mockito.when(em.createNamedQuery(Matchers.<String>any())).thenReturn(aclTypedQuery);

        documentManagerBean.removeACLFromDocumentRevision(documentRevision.getKey());
        Assert.assertTrue(documentRevision.getACL() == null);
    }

    public void removeTagFromDocument(){

    }
}