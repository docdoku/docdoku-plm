package com.docdoku.server.documents;

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.DocumentBaseline;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.Folder;
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

    @Spy
    Workspace workspace = new Workspace("workspace01");
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
        Assert.assertTrue(documentBaseline.getDescription().equals("description"));
        Assert.assertTrue(documentBaseline.hasBasedLinedFolder(workspace.getId()));
        Assert.assertTrue(documentBaseline.getBaselinedFolders().size() == 0);
        Assert.assertTrue(documentBaseline.getWorkspace().getId().equals(workspace.getId()));
    }


    /**
     * test that we cannot baseline a workspace that contains documents not checkin yet
     * @throws Exception
     */
    @Test
    public void shouldNotBaselineNotCheckedDocuments() throws Exception {
        //Given
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(folder.getCompletePath()).thenReturn("workspace01");
        Mockito.when(em.find(Workspace.class, workspace.getId())).thenReturn(workspace);
        Mockito.when(em.createQuery("SELECT DISTINCT f FROM Folder f WHERE f.parentFolder.completePath = :completePath", Folder.class)).thenReturn(folderTypedQuery);
        Mockito.when(folderTypedQuery.getResultList()).thenReturn(new ArrayList<Folder>(0));
        Mockito.when(new WorkspaceDAO(new Locale("en"), em).loadWorkspace(workspace.getId())).thenReturn(workspace);
        Mockito.when(em.find(Folder.class,workspace.getId())).thenReturn(folder);
        DocumentRevision[] revisions= new DocumentRevision[1];
        DocumentRevision documentRevision = new DocumentRevision();
        DocumentMaster documentMaster = Mockito.mock(DocumentMaster.class);
        documentMaster.setId("doc001");
        documentRevision.setDocumentMaster(documentMaster);
        documentRevision.setCheckOutUser(user);
        Mockito.when(documentService.getAllDocumentsInWorkspace(workspace.getId())).thenReturn(revisions);

        //when
        DocumentBaseline documentBaseline= docBaselineManagerBean.createBaseline(workspace.getId(), "name", "description");

        //Then
        Assert.assertTrue(documentBaseline == null);
        Assert.assertTrue(documentBaseline.hasBasedLinedFolder(workspace.getId()));
        Assert.assertTrue(documentBaseline.getBaselinedFolders().size() == 0);
        Assert.assertTrue(documentBaseline.getWorkspace().getId().equals(workspace.getId()));
    }


    @Test
    public void testGetBaselines() throws Exception {

    }

    @Test
    public void testDeleteBaseline() throws Exception {

    }

    @Test
    public void testGetBaseline() throws Exception {

    }
}