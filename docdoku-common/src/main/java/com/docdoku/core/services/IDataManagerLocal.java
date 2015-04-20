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

package com.docdoku.core.services;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.FileNotFoundException;
import com.docdoku.core.exceptions.StorageException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public interface IDataManagerLocal {
    InputStream getBinaryResourceInputStream(BinaryResource binaryResource) throws StorageException;
    InputStream getBinarySubResourceInputStream(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException;
    OutputStream getBinaryResourceOutputStream(BinaryResource binaryResource) throws StorageException;
    OutputStream getBinarySubResourceOutputStream(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException;
    boolean exists(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException;
    void copyData(BinaryResource source, BinaryResource destination) throws StorageException;
    void deleteData(BinaryResource binaryResource) throws StorageException;
    void renameFile(BinaryResource binaryResource, String pNewName) throws StorageException, FileNotFoundException;
    Date getLastModified(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException;
    String getExternalStorageURI(BinaryResource binaryResource);
    String getShortenExternalStorageURI(BinaryResource binaryResource);
    void deleteWorkspaceFolder(String workspaceId, List<BinaryResource> binaryResourcesInWorkspace) throws StorageException;
}
