package com.docdoku.api;


import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.FolderDTO;
import com.docdoku.api.services.FoldersApi;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class FoldersApiTest {

    @Test
    public void getRootFoldersTest() throws ApiException {
        FoldersApi foldersApi = new FoldersApi(TestConfig.BASIC_CLIENT);
        List<FolderDTO> rootFolders = foldersApi.getRootFolders(TestConfig.WORKSPACE, null);
        Assert.assertNotNull(rootFolders);
    }

    @Test
    public void createRootFoldersTest() throws ApiException {
        String folderName = "Folder-"+UUID.randomUUID().toString().substring(0,6);
        FolderDTO folder = new FolderDTO();
        folder.setName(folderName);
        FoldersApi foldersApi = new FoldersApi(TestConfig.BASIC_CLIENT);
        foldersApi.createSubFolder(TestConfig.WORKSPACE,TestConfig.WORKSPACE, folder);
        List<FolderDTO> rootFolders = foldersApi.getRootFolders(TestConfig.WORKSPACE, null);
        Assert.assertEquals(rootFolders.stream()
                .filter(folderDTO -> folderName.equals(folderDTO.getName()))
                .count(),1);
    }
}
