package com.docdoku.server.example.api;

import com.docdoku.server.api.AccountsApi;
import com.docdoku.server.api.FoldersApi;
import com.docdoku.server.api.WorkspacesApi;
import com.docdoku.server.client.DocdokuPLMClient;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.model.AccountDTO;
import io.swagger.client.model.FolderDTO;
import io.swagger.client.model.WorkspaceDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 */
public class UseApi {

    private final static String API_URL = "http://localhost:8080/api";
    private final static String USERNAME = "user";
    private final static String PASSWORD = "password";
    private final static String WORKSPACE = "user-workspace";
    private final static String FOLDERA = "FA";
    private final static String FOLDERB = "FB";
    private final static boolean DEBUG = true;

    private static final Logger LOGGER = Logger.getLogger(UseApi.class.getName());

    public static void main(String[] args) throws UnsupportedEncodingException {

        DocdokuPLMClient plmClient = new DocdokuPLMClient(API_URL, USERNAME, PASSWORD, DEBUG);
        ApiClient client = plmClient.getClient();

        AccountsApi accountsApi = new AccountsApi(client);

        try {
            AccountDTO account = accountsApi.getAccount();
            System.out.println(account);
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Error " + client.getStatusCode() , e);
        }

        try {
            List<WorkspaceDTO> workspaces = accountsApi.getWorkspaces();
            System.out.println(workspaces);
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Error " + client.getStatusCode() , e);
        }

        WorkspacesApi workspacesApi = new WorkspacesApi(client);
        WorkspaceDTO workspaceDTO = new WorkspaceDTO();
        workspaceDTO.setId(WORKSPACE);

        try {
            workspacesApi.createWorkspace(workspaceDTO,"user");
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Error " + client.getStatusCode() , e);

        }

        FoldersApi foldersApi = new FoldersApi(client);
        try {
            List<FolderDTO> rootFolders = foldersApi.getRootFolders(WORKSPACE, null);
            System.out.println(rootFolders);
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Error " + client.getStatusCode(), e);
        }

        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setName(FOLDERB);

        try {
            foldersApi.createSubFolder(WORKSPACE, WORKSPACE+":"+FOLDERA, folderDTO);
        } catch (ApiException e) {
            LOGGER.log(Level.SEVERE, "Error " + client.getStatusCode(), e);
        }



    }

}
