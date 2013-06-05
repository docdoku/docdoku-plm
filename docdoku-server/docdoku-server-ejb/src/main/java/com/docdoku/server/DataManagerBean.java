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
package com.docdoku.server;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.FileNotFoundException;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.StorageException;
import com.docdoku.server.storage.StorageProvider;
import com.docdoku.server.storage.filesystem.FileStorageProvider;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.Local;
import javax.ejb.Stateless;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@DeclareRoles("users")
@Local(IDataManagerLocal.class)
@Stateless(name = "DataManagerBean")
public class DataManagerBean implements IDataManagerLocal {

    @Resource(name = "vaultPath")
    private String vaultPath;

    private StorageProvider defaultStorageProvider;
    private FileStorageProvider fileStorageProvider;

    private final static Logger LOGGER = Logger.getLogger(DataManagerBean.class.getName());

    @PostConstruct
    private void init() {
        //defaultStorageProvider = new GoogleStorageProvider("");
        fileStorageProvider = new FileStorageProvider(vaultPath);
        defaultStorageProvider = fileStorageProvider;
    }

    @Override
    public InputStream getBinaryResourceInputStream(BinaryResource binaryResource) throws StorageException {
        try {
            return defaultStorageProvider.getBinaryResourceInputStream(binaryResource);
        } catch (FileNotFoundException e) {
            BinaryResource previous = binaryResource.getPrevious();
            if (previous != null)
                return getBinaryResourceInputStream(previous);
            else {
                throw new StorageException(new StringBuilder().append("Can't find ").append(binaryResource.getFullName()).toString());
            }
        }
    }

    @Override
    public InputStream getBinarySubResourceInputStream(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException {
        try {
            return fileStorageProvider.getBinarySubResourceInputStream(binaryResource, subResourceVirtualPath);
        } catch (FileNotFoundException e) {
            BinaryResource previous = binaryResource.getPrevious();
            if (previous != null)
                return getBinarySubResourceInputStream(previous, subResourceVirtualPath);
            else {
                throw new StorageException(new StringBuilder().append("Can't find sub resource ").append(subResourceVirtualPath).append(" of ").append(binaryResource.getFullName()).toString());
            }
        }
    }

    @Override
    public OutputStream getBinaryResourceOutputStream(BinaryResource binaryResource) throws StorageException {
        return defaultStorageProvider.getBinaryResourceOutputStream(binaryResource);
    }

    @Override
    public OutputStream getBinarySubResourceOutputStream(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException {
        return fileStorageProvider.getBinarySubResourceOutputStream(binaryResource, subResourceVirtualPath);
    }

    @Override
    public boolean exists(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException {
        if (fileStorageProvider.exists(binaryResource, subResourceVirtualPath)) {
            return true;
        } else {
            BinaryResource previous = binaryResource.getPrevious();
            if (previous != null) {
                return exists(previous, subResourceVirtualPath);
            } else {
                return false;
            }
        }
    }

    @Override
    public void copyData(BinaryResource source, BinaryResource destination) throws StorageException {
        try {
            defaultStorageProvider.copyData(source, destination);
            fileStorageProvider.copySubResources(source, destination);
        } catch (FileNotFoundException e) {
            BinaryResource previous = source.getPrevious();
            if (previous != null)
                copyData(previous, destination);
            else {
                throw new StorageException(new StringBuilder().append("Can't find source file to copy ").append(source.getFullName()).toString());
            }
        }
    }

    @Override
    public void deleteData(BinaryResource binaryResource) throws StorageException {
        defaultStorageProvider.delData(binaryResource);
        fileStorageProvider.deleteSubResources(binaryResource);
        fileStorageProvider.cleanParentFolders(binaryResource);
    }

    @Override
    public Date getLastModified(BinaryResource binaryResource, String subResourceVirtualPath) throws StorageException {
        try {
            return fileStorageProvider.getLastModified(binaryResource, subResourceVirtualPath);
        } catch (FileNotFoundException e) {
            BinaryResource previous = binaryResource.getPrevious();
            if (previous != null)
                return getLastModified(previous, subResourceVirtualPath);
            else {
                throw new StorageException(new StringBuilder().append("Can't find source file to get last modified date ").append(binaryResource.getFullName()).toString());
            }
        }
    }

    @Override
    public void deleteWorkspaceFolder(String workspaceId, List<BinaryResource> binaryResourcesInWorkspace) throws StorageException {
        defaultStorageProvider.deleteWorkspaceFolder(workspaceId, binaryResourcesInWorkspace);
    }

}
