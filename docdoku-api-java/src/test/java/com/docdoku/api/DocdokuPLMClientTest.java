package com.docdoku.api;

import com.docdoku.api.client.ApiClient;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.models.WorkspaceListDTO;
import com.docdoku.api.services.AccountsApi;
import com.docdoku.api.services.WorkspacesApi;
import junit.framework.Assert;
import org.junit.Ignore;
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
    @Test
    public void basicTests() throws ApiException {
        runTest(TestConfig.BASIC_CLIENT);
    }

    @Test
    public void cookieTests() throws ApiException {
        runTest(TestConfig.COOKIE_CLIENT);
    }

    @Test
    @Ignore
    public void jwtTests() throws ApiException {
        runTest(TestConfig.JWT_CLIENT);
    }


    private void runTest(ApiClient client) throws ApiException {
        WorkspacesApi workspacesApi = new WorkspacesApi(client);
        AccountsApi accountsApi = new AccountsApi(client);

        AccountDTO account = accountsApi.getAccount();
        Assert.assertEquals(TestConfig.LOGIN, account.getLogin());

        WorkspaceDTO workspace = new WorkspaceDTO();
        workspace.setId(UUID.randomUUID().toString());

        workspacesApi.createWorkspace(workspace, TestConfig.LOGIN);

        WorkspaceListDTO workspacesForConnectedUser = workspacesApi.getWorkspacesForConnectedUser();
        Assert.assertNotNull(workspacesForConnectedUser);
        Assert.assertTrue("Should contain created workspace", workspacesForConnectedUser.getAllWorkspaces().contains(workspace));
    }

}
