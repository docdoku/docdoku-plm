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

package com.docdoku.server.storage;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.FileNotFoundException;
import com.docdoku.core.exceptions.StorageException;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StorageProvider {
    InputStream getBinaryResourceInputStream(BinaryResource pBinaryResource) throws StorageException, FileNotFoundException;
    File getBinaryResourceFile(BinaryResource pBinaryResource) throws StorageException, FileNotFoundException;
    OutputStream getBinaryResourceOutputStream(BinaryResource pBinaryResource) throws StorageException;
    void copyData(BinaryResource pSourceBinaryResource, BinaryResource pTargetBinaryResource) throws StorageException, FileNotFoundException;
    File copyFile(File file, BinaryResource pTargetBinaryResource) throws StorageException, FileNotFoundException;
    void delData(BinaryResource pBinaryResource) throws StorageException;
    String getExternalResourceURI(BinaryResource binaryResource);
    String getShortenExternalResourceURI(BinaryResource binaryResource);
    void deleteWorkspaceFolder(String workspaceId, List<BinaryResource> binaryResourcesInWorkspace) throws StorageException;
    void renameData(File file, String pNewName) throws StorageException;
}
