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
        String folderName = "Folder-"+UUID.randomUUID().toString().substring(0, 8);
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
