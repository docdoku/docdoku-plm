package com.docdoku.server.example.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.services.WorkspacesApi;
import com.docdoku.server.example.utils.ErrorHelper;

import java.util.List;


/**
 * This class calls some WorkspacesApi methods
 * @Author Morgan Guimard
 */
public class WorkspacesApiExample extends DocdokuPLMApiExample {

    private WorkspacesApi workspacesApi;
    private final static String USER_GROUP_ID = "Group1";
    private final static String USER_2_LOGIN = "bar";

    @Override
    public void run() {
        workspacesApi = new WorkspacesApi(plmClient.getClient());
        createWorkspace();
        createUserGroup();
        addUserInWorkspace();
        grantAccessToUserInWorkspace();
        getWorkspaceDetailsList();
        listDocumentsInWorkspace();
        updateWorkspaceDescription();
    }

    private void grantAccessToUserInWorkspace() {
        try {
            UserDTO userToGrant = new UserDTO();
            userToGrant.setLogin(USER_2_LOGIN);
            userToGrant.setMembership(UserDTO.MembershipEnum.FULL_ACCESS);
            workspacesApi.setUserAccess(WORKSPACE, userToGrant);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while granting user 2 in workspace", plmClient.getClient());
        }
    }

    private void addUserInWorkspace() {
        try {
            UserDTO userToCreate = new UserDTO();
            userToCreate.setLogin(USER_2_LOGIN);
            workspacesApi.addUser(WORKSPACE, userToCreate, null);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while adding user 2 in workspace", plmClient.getClient());
        }
    }

    private void createUserGroup() {
        try {
            UserGroupDTO groupToCreate = new UserGroupDTO();
            groupToCreate.setId(USER_GROUP_ID);
            groupToCreate.setWorkspaceId(WORKSPACE);
            workspacesApi.createGroup(WORKSPACE, groupToCreate);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while creating user group", plmClient.getClient());
        }
    }

    private void updateWorkspaceDescription() {
        try {
            WorkspaceDTO workspaceDTO = new WorkspaceDTO();
            workspaceDTO.setId(WORKSPACE);
            workspaceDTO.setDescription("Updated description");
            workspacesApi.updateWorkspace(WORKSPACE, workspaceDTO);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while updating workspace", plmClient.getClient());
        }
    }

    private void listDocumentsInWorkspace() {
        try {
            List<DocumentRevisionDTO> documentsInWorkspace = workspacesApi.getDocumentsInWorkspace(WORKSPACE, 0, null);
            System.out.println(documentsInWorkspace);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while listing documents in workspace", plmClient.getClient());
        }
    }


    private void createWorkspace(){
        try {
            WorkspaceDTO workspaceDTO = new WorkspaceDTO();
            workspaceDTO.setId(WORKSPACE);
            workspacesApi.createWorkspace(workspaceDTO,"user");
        } catch (ApiException e) {
            ErrorHelper.onError("Error while creating workspace", plmClient.getClient());
        }
    }


    private void getWorkspaceDetailsList() {
        try {
            List<WorkspaceDetailsDTO> detailedWorkspacesForConnectedUser = workspacesApi.getDetailedWorkspacesForConnectedUser();
            System.out.println(detailedWorkspacesForConnectedUser);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while getting detailed workspace list", plmClient.getClient());
        }
    }

}
