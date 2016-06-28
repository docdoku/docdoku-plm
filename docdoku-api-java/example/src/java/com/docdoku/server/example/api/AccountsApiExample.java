package com.docdoku.server.example.api;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.AccountDTO;
import com.docdoku.api.models.WorkspaceDTO;
import com.docdoku.api.services.AccountsApi;
import com.docdoku.server.example.utils.ErrorHelper;

import java.util.List;

/**
 * This class calls some AccountsApi methods
 * @Author Morgan Guimard
 */
public class AccountsApiExample extends DocdokuPLMApiExample {

    private AccountsApi accountsApi;

    @Override
    public void run() {
        accountsApi = new AccountsApi(plmClient.getClient());
        showMyAccount();
        listWorkspacesIBelong();
    }

    private void showMyAccount(){
        try {
            AccountDTO account = accountsApi.getAccount();
            System.out.println(account);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while getting account", plmClient.getClient());
        }
    }

    private void listWorkspacesIBelong(){
        try {
            List<WorkspaceDTO> workspaces = accountsApi.getWorkspaces();
            System.out.println(workspaces);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while getting workspace list", plmClient.getClient());
        }
    }

}
