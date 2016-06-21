package com.docdoku.server.api;

import com.docdoku.server.api.client.ApiClient;
import com.docdoku.server.api.client.ApiException;
import com.docdoku.server.api.models.AccountDTO;
import com.docdoku.server.api.models.WorkspaceDTO;
import com.docdoku.server.api.models.WorkspaceListDTO;
import com.docdoku.server.api.services.AccountsApi;
import com.docdoku.server.api.services.WorkspacesApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.UUID;

/**
 * This class tests DocdokuPLMClient class
 * @Author Morgan Guimard
 */

@RunWith(JUnit4.class)

public class DocdokuPLMClientTest {

    private final static String URL = "http://localhost:8080/api";
    private final static String LOGIN = "foo";
    private final static String PASSWORD = "bar";

    @Test
    public void basicTests() throws ApiException {
        DocdokuPLMClient docdokuPLMClient = new DocdokuPLMBasicClient(URL, LOGIN, PASSWORD, true);
        runTest(docdokuPLMClient.getClient());
    }

    @Test
    public void cookieTests() throws ApiException {
        DocdokuPLMClient docdokuPLMClient = new DocdokuPLMCookieClient(URL, LOGIN, PASSWORD, true);
        runTest(docdokuPLMClient.getClient());
    }

    @Test
    public void jwtTests() throws ApiException {
        DocdokuPLMClient docdokuPLMClient = new DocdokuPLMJWTClient(URL, LOGIN, PASSWORD, true);
        runTest(docdokuPLMClient.getClient());
    }


    private void runTest(ApiClient client) throws ApiException {
        WorkspacesApi workspacesApi = new WorkspacesApi(client);
        AccountsApi accountsApi = new AccountsApi(client);

        AccountDTO account = accountsApi.getAccount();
        Assert.assertEquals(LOGIN, account.getLogin());

        WorkspaceDTO workspace = new WorkspaceDTO();
        workspace.setId(UUID.randomUUID().toString());

        workspacesApi.createWorkspace(workspace, LOGIN);

        WorkspaceListDTO workspacesForConnectedUser = workspacesApi.getWorkspacesForConnectedUser();
        Assert.assertNotNull(workspacesForConnectedUser);
        Assert.assertTrue("Should contain created workspace", workspacesForConnectedUser.getAllWorkspaces().contains(workspace));
    }

}
