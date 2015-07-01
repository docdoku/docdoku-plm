package com.docdoku.server;

import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.meta.NameValuePair;
import com.docdoku.core.services.IUserManagerLocal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by lebeaujulien on 12/03/15.
 */
public class LOVManagerBeanTest extends LOVManagerBean {

    public static final String WORKSPACE_ID="TestWorkspace";
    public static final String USER_LOGIN="User1";
    public static final String USER_NAME="UserName";
    public static final String USER_MAIL="UserMail@mail.com";
    public static final String USER_LANGUAGE="fr";

    @InjectMocks
    LOVManagerBean lovManager;
    @Mock
    private IUserManagerLocal userManager;
    @Mock
    private EntityManager em;

    private Account account;
    private Workspace workspace ;
    private User user;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        account = new Account(USER_LOGIN, USER_NAME, USER_MAIL, USER_LANGUAGE, new Date(), null);
        workspace = new Workspace(WORKSPACE_ID,account, "pDescription", false);
        user = new User(workspace, USER_LOGIN , USER_LOGIN, USER_MAIL,USER_LANGUAGE);
    }

    @Test
    public void creationNominal() throws Exception {
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(Workspace.class,workspace.getId())).thenReturn(workspace);

        List<NameValuePair> possibleValue = new ArrayList<>();
        possibleValue.add(new NameValuePair("first", "value"));

        lovManager.createLov(workspace.getId(), "NominalName", possibleValue);
    }

    @Test (expected = CreationException.class)
    public void creationNoName() throws Exception {
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(Workspace.class,workspace.getId())).thenReturn(workspace);


        List<NameValuePair> possibleValue = new ArrayList<>();
        possibleValue.add(new NameValuePair("first", "value"));

        lovManager.createLov(workspace.getId(), null, possibleValue);
    }

    @Test (expected = CreationException.class)
    public void creationNoPossibleValue() throws Exception {
        Mockito.when(userManager.checkWorkspaceWriteAccess(workspace.getId())).thenReturn(user);
        Mockito.when(userManager.checkWorkspaceReadAccess(workspace.getId())).thenReturn(user);
        Mockito.when(em.find(Workspace.class,workspace.getId())).thenReturn(workspace);

        lovManager.createLov(workspace.getId(), "Name", null);
    }
}
