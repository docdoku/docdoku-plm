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

package com.docdoku.server.documents;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.document.*;
import com.docdoku.core.security.ACL;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.server.DataManagerBean;
import com.docdoku.server.DocumentManagerBean;
import com.docdoku.server.UserManagerBean;
import com.docdoku.server.dao.ConfigurationItemDAO;
import com.docdoku.server.dao.FolderDAO;
import com.docdoku.server.dao.WorkspaceDAO;
import org.apache.poi.ss.formula.functions.Match;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentBaselineManagerBeanTest {

    @InjectMocks
    DocumentBaselineManagerBean docBaselineManagerBean = new DocumentBaselineManagerBean();
    @Mock
    IDocumentManagerLocal documentManagerLocal;
    @Mock
    IUserManagerLocal userManager;
    @Mock
    EntityManager em;
    @Mock
    DataManagerBean dataManager;
    Account account = Mockito.spy(new Account("user2" , "user2", "user2@docdoku.com", "en",new Date(),null));
    @Spy
    Workspace workspace = new Workspace("workspace01",account, "pDescription", false);
    @Spy
    User user = new User(workspace, "user1" , "user1", "user1@docdoku.com", "en");

    Folder folder = Mockito.mock(Folder.class);

    @Mock
    TypedQuery<Folder> folderTypedQuery;
    @Mock
    private IDocumentManagerLocal documentService;

    /**
     * test that we cannot baseline an empty workspace
     * @throws Exception
     */
    @Test
    public void shouldNotBaselineEmptyWorkspace() throws Exception {
        //Given
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(folder.getCompletePath()).thenReturn("workspace01");
        Mockito.when(em.find(Workspace.class, workspace.getId())).thenReturn(workspace);
        Mockito.when(em.createQuery("SELECT DISTINCT f FROM Folder f WHERE f.parentFolder.completePath = :completePath", Folder.class)).thenReturn(folderTypedQuery);
        Mockito.when(folderTypedQuery.getResultList()).thenReturn(new ArrayList<Folder>(0));
        Mockito.when(new WorkspaceDAO(new Locale("en"), em).loadWorkspace(workspace.getId())).thenReturn(workspace);
        Mockito.when(em.find(Folder.class,workspace.getId())).thenReturn(folder);
        Mockito.when(documentService.getAllDocumentsInWorkspace(workspace.getId())).thenReturn(new DocumentRevision[0]);

        //when
        DocumentBaseline documentBaseline= docBaselineManagerBean.createBaseline(workspace.getId(), "name", "description");

        //Then
        //TODO should test documentBaseline is null after code update
        Assert.assertTrue(documentBaseline.getDescription().equals("description"));
        Assert.assertTrue(documentBaseline.hasBasedLinedFolder(workspace.getId()));
        Assert.assertTrue(documentBaseline.getBaselinedFolders().size() == 1);
        Assert.assertTrue(documentBaseline.getWorkspace().getId().equals(workspace.getId()));
    }


    /**
     * test that we can baseline documents only when we have read permission
     * @throws Exception
     */
    @Test
    public void baselineDocuments() throws Exception {

        //Given
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(workspace.getAdmin()).thenReturn(account);
        Mockito.when(folder.getCompletePath()).thenReturn("workspace01");
        Mockito.when(em.find(Workspace.class, workspace.getId())).thenReturn(workspace);
        Mockito.when(em.createQuery("SELECT DISTINCT f FROM Folder f WHERE f.parentFolder.completePath = :completePath", Folder.class)).thenReturn(folderTypedQuery);
        Mockito.when(folderTypedQuery.getResultList()).thenReturn(new ArrayList<Folder>(0));
        Mockito.when(new WorkspaceDAO(new Locale("en"), em).loadWorkspace(workspace.getId())).thenReturn(workspace);
        Mockito.when(em.find(Folder.class,workspace.getId())).thenReturn(folder);
        DocumentRevision[] revisions= new DocumentRevision[2];

        DocumentMaster documentMaster = Mockito.spy(new DocumentMaster(workspace,"doc1",user));
        DocumentMaster documentMaster2 = Mockito.spy(new DocumentMaster(workspace,"doc2",user));
        documentMaster.setId("doc001");
        DocumentRevision documentRevision = Mockito.spy(new DocumentRevision(documentMaster,user));
        DocumentRevision documentRevision2 = Mockito.spy(new DocumentRevision(documentMaster2,user));
        documentRevision.setDocumentMaster(documentMaster);
        documentRevision.setCheckOutUser(user);
        documentRevision2.setDocumentMaster(documentMaster2);
        documentRevision2.setCheckOutUser(user);
        documentRevision2.setDocumentIterations(new ArrayList<DocumentIteration>(1));
        documentRevision.setDocumentIterations(new ArrayList<DocumentIteration>(1));

        ACL acl = Mockito.spy(new ACL());
        ACL acl2 = Mockito.spy(new ACL());
        acl.addEntry(user, ACL.Permission.FORBIDDEN);
        acl2.addEntry(user, ACL.Permission.READ_ONLY);
        Mockito.when(documentRevision.getACL()).thenReturn(acl);
        Mockito.when(documentRevision2.getACL()).thenReturn(acl2);

        revisions[0] = documentRevision2;
        revisions[1] = documentRevision;
        documentRevision2.createNextIteration(user);
        documentRevision.createNextIteration(user);
        //Mockito.when(documentRevision.getLastIteration()).thenReturn(new DocumentIteration(documentRevision, user));
        //Mockito.when(documentRevision2.getLastIteration()).thenReturn(new DocumentIteration(documentRevision2, user));
        Mockito.when(documentRevision.getLocation()).thenReturn(folder);
        Mockito.when(documentRevision2.getLocation()).thenReturn(folder);

        Mockito.when(em.find(DocumentRevision.class, documentRevision.getKey())).thenReturn(documentRevision);
        Mockito.when(em.find(DocumentRevision.class, documentRevision2.getKey())).thenReturn(documentRevision2);

        Mockito.when(documentService.getAllDocumentsInWorkspace(workspace.getId())).thenReturn(revisions);
        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);

        //when
        DocumentBaseline documentBaseline= docBaselineManagerBean.createBaseline(workspace.getId(), "name", "description");

        //Then
        Assert.assertTrue(documentBaseline != null);
        Assert.assertTrue(documentBaseline.hasBasedLinedFolder(workspace.getId()));
        Assert.assertTrue(documentBaseline.getBaselinedFolders().size() == 1);
        Assert.assertTrue(documentBaseline.getBaselinedDocument(documentRevision.getKey()) == null);
        Assert.assertTrue(documentBaseline.getBaselinedDocument(documentRevision2.getKey()) != null);


    }
}