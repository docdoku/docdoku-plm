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

/**
 * @author Asmae Chadid
 */
public class GoogleStorageProvider implements StorageProvider {

    private final String vaultPath;

    public GoogleStorageProvider(String vaultPath) {
        this.vaultPath = vaultPath;
    }

    @Override
    public String getVirtualPath(BinaryResource pBinaryResource) {
        String normalizedName = Tools.unAccent(pBinaryResource.getFullName());
        if (vaultPath != null && !vaultPath.isEmpty()) {
            return new StringBuilder().append(vaultPath).append("/").append(normalizedName).toString();
        } else {
            return normalizedName;
        }
    }

    public String getVirtualParentPath(BinaryResource pBinaryResource) {
        String virtualPath = getVirtualPath(pBinaryResource);
        int index = virtualPath.lastIndexOf("/");
        return virtualPath.substring(0, index);
    }

    @Override
    public InputStream getBinaryContentInputStream(BinaryResource pBinaryResource) throws StorageException, FileNotFoundException {
        return GoogleStorageCloud.getInputStream(getVirtualPath(pBinaryResource));
    }

    @Override
    public InputStream getBinaryContentInputStream(BinaryResource pBinaryResource, String subResourceVirtualPath) throws StorageException, FileNotFoundException {
        String virtualPath = new StringBuilder().append(getVirtualParentPath(pBinaryResource)).append("/").append(subResourceVirtualPath).toString();
        return GoogleStorageCloud.getInputStream(virtualPath);
    }

    @Override
    public OutputStream getOutputStream(BinaryResource pBinaryResource) throws StorageException {
        return GoogleStorageCloud.getOutputStream(getVirtualPath(pBinaryResource));
    }

    @Override
    public OutputStream getOutputStream(BinaryResource pBinaryResource, String subResourceVirtualPath) throws StorageException {
        String virtualPath = new StringBuilder().append(getVirtualParentPath(pBinaryResource)).append("/").append(subResourceVirtualPath).toString();
        return GoogleStorageCloud.getOutputStream(virtualPath);
    }

    @Override
    public void copyData(BinaryResource pSourceBinaryResource, BinaryResource pTargetBinaryResource) throws StorageException, FileNotFoundException {
        GoogleStorageCloud.copy(getVirtualPath(pSourceBinaryResource), getVirtualPath(pTargetBinaryResource));
    }

    @Override
    public void delData(BinaryResource pBinaryResource) throws StorageException{
        GoogleStorageCloud.delete(getVirtualPath(pBinaryResource));
    }

}
