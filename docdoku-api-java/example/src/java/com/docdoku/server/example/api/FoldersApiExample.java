package com.docdoku.server.example.api;


import com.docdoku.server.api.client.ApiException;
import com.docdoku.server.api.models.DocumentCreationDTO;
import com.docdoku.server.api.models.DocumentRevisionDTO;
import com.docdoku.server.api.models.FolderDTO;
import com.docdoku.server.api.services.FoldersApi;
import com.docdoku.server.example.utils.ErrorHelper;

import java.util.List;

/**
 * This class calls some FoldersApi methods
 * @Author Morgan Guimard
 */
public class FoldersApiExample extends DocdokuPLMApiExample {

    private FoldersApi foldersApi;
    private final static String DOC_REF = "DOC1";
    private final static String FOLDERA = "FA";
    private final static String FOLDERB = "FB";

    @Override
    public void run() {
        foldersApi = new FoldersApi(plmClient.getClient());
        listRootFolders();
        createFolderA();
        createDocument();
        createFolderB();
        listRootFolders();
        listDocumentsInFolder();
    }

    private void listDocumentsInFolder() {
        try {
            List<DocumentRevisionDTO> documents = foldersApi.getDocumentsWithGivenFolderIdAndWorkspaceId(WORKSPACE, WORKSPACE + ":" + FOLDERA, null);
            System.out.println(documents);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while getting detailed workspace list", plmClient.getClient());
        }
    }

    private void createFolderA() {
        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setName(FOLDERA);
        try {
            foldersApi.createSubFolder(WORKSPACE, WORKSPACE, folderDTO);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while creating folder A", plmClient.getClient());
        }
    }
    private void createFolderB() {
        FolderDTO folderDTO = new FolderDTO();
        folderDTO.setName(FOLDERB);
        try {
            foldersApi.createSubFolder(WORKSPACE, WORKSPACE+":"+FOLDERA, folderDTO);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while creating folder B", plmClient.getClient());
        }
    }

    private void listRootFolders() {
        try {
            List<FolderDTO> rootFolders = foldersApi.getRootFolders(WORKSPACE, null);
            System.out.println(rootFolders);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while listing root folders", plmClient.getClient());
        }
    }

    private void createDocument(){
        try {
            DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
            documentCreationDTO.setWorkspaceId(WORKSPACE);
            documentCreationDTO.setReference(DOC_REF);
            documentCreationDTO.setTitle(DOC_REF);
            documentCreationDTO.setDescription(DOC_REF);
            foldersApi.createDocumentMasterInFolder(WORKSPACE, documentCreationDTO,WORKSPACE+":"+FOLDERA,null);
        } catch (ApiException e) {
            ErrorHelper.onError("Error while creating document", plmClient.getClient());
        }
    }

}
