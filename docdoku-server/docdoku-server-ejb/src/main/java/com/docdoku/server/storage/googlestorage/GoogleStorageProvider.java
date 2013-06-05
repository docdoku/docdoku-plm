/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.server.storage.googlestorage;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.FileNotFoundException;
import com.docdoku.core.services.StorageException;
import com.docdoku.core.util.Tools;
import com.docdoku.server.storage.StorageProvider;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Asmae Chadid
 */
public class GoogleStorageProvider implements StorageProvider {

    private final String vaultPath;

    public GoogleStorageProvider(String vaultPath) {
        this.vaultPath = vaultPath;
    }

    private String getVirtualPath(BinaryResource pBinaryResource) {
        String normalizedName = Tools.unAccent(pBinaryResource.getFullName());
        if (vaultPath != null && !vaultPath.isEmpty()) {
            return new StringBuilder().append(vaultPath).append("/").append(normalizedName).toString();
        } else {
            return normalizedName;
        }
    }

    @Override
    public InputStream getBinaryResourceInputStream(BinaryResource pBinaryResource) throws StorageException, FileNotFoundException {
        return GoogleStorageCloud.getInputStream(getVirtualPath(pBinaryResource));
    }

    @Override
    public OutputStream getBinaryResourceOutputStream(BinaryResource pBinaryResource) throws StorageException {
        return GoogleStorageCloud.getOutputStream(getVirtualPath(pBinaryResource));
    }

    @Override
    public void copyData(BinaryResource pSourceBinaryResource, BinaryResource pTargetBinaryResource) throws StorageException, FileNotFoundException {
        GoogleStorageCloud.copy(getVirtualPath(pSourceBinaryResource), getVirtualPath(pTargetBinaryResource));
    }

    @Override
    public void delData(BinaryResource pBinaryResource) throws StorageException{
        GoogleStorageCloud.delete(getVirtualPath(pBinaryResource));
    }

    @Override
    public void deleteWorkspaceFolder(String workspaceId, List<BinaryResource> binaryResourcesInWorkspace) throws StorageException {
        if(workspaceId != null && workspaceId != ""){
            for(BinaryResource br : binaryResourcesInWorkspace) {
                delData(br);
            }
        }
    }

}
