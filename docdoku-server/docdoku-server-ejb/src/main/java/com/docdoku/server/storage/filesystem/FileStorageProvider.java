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

package com.docdoku.server.storage.filesystem;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.FileNotFoundException;
import com.docdoku.core.exceptions.StorageException;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.util.Tools;
import com.docdoku.server.storage.StorageProvider;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FileStorageProvider implements StorageProvider {

    private final String vaultPath;

    private static final Logger LOGGER = Logger.getLogger(StorageProvider.class.getName());

    public FileStorageProvider(String vaultPath) {
        this.vaultPath = vaultPath;
    }


    @Override
    public InputStream getBinaryResourceInputStream(BinaryResource pBinaryResource) throws StorageException, FileNotFoundException {
        File file = new File(getVirtualPath(pBinaryResource));
        return getInputStream(file);
    }

    @Override
    public File getBinaryResourceFile(BinaryResource pBinaryResource) throws StorageException, FileNotFoundException {
        File file = new File(getVirtualPath(pBinaryResource));
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.close();
                return file;
            } catch (IOException e) {
                throw new StorageException(e.getMessage(), e);
            }
        } else {
            throw new FileNotFoundException(new StringBuilder().append(file.getAbsolutePath()).append(" not found").toString());
        }
    }

    @Override
    public OutputStream getBinaryResourceOutputStream(BinaryResource pBinaryResource) throws StorageException {
        File file = new File(getVirtualPath(pBinaryResource));
        file.getParentFile().mkdirs();
        try {
            return new BufferedOutputStream(new FileOutputStream(file));
        } catch (java.io.FileNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }


    @Override
    public void copyData(BinaryResource pSourceBinaryResource, BinaryResource pTargetBinaryResource) throws StorageException, FileNotFoundException  {
        File source = new File(getVirtualPath(pSourceBinaryResource));
        if (source.exists()) {
            File target = new File(getVirtualPath(pTargetBinaryResource));
            try {
                FileIO.copyFile(source, target);
            } catch (IOException e) {
                throw new StorageException(new StringBuilder().append("Error copying ").append(pSourceBinaryResource.getFullName()).append(" to ").append(pTargetBinaryResource.getFullName()).toString(), e);
            }
        } else {
            throw new FileNotFoundException(new StringBuilder("Can't find source file to copy ").append(pSourceBinaryResource.getFullName()).toString());
        }
    }

    @Override
    public boolean exists(BinaryResource binaryResource, String generatedFileName) {
        File generatedFile = new File(getGeneratedFilesFolder(binaryResource), Tools.unAccent(generatedFileName));
        return generatedFile.exists();
    }

    @Override
    public Date getLastModified(BinaryResource binaryResource, String generatedFileName) throws FileNotFoundException {
        File generatedFile = new File(getGeneratedFilesFolder(binaryResource), Tools.unAccent(generatedFileName));
        if (generatedFile.exists()) {
            return new Date(generatedFile.lastModified());
        } else {
            throw new FileNotFoundException(new StringBuilder("Can't find source file to get last modified date ").append(binaryResource.getFullName()).toString());
        }
    }

    @Override
    public InputStream getGeneratedFileInputStream(BinaryResource pBinaryResource, String generatedFileName) throws StorageException, FileNotFoundException {
        File generatedFile = new File(getGeneratedFilesFolder(pBinaryResource), Tools.unAccent(generatedFileName));
        return getInputStream(generatedFile);
    }

    @Override
    public OutputStream getGeneratedFileOutputStream(BinaryResource binaryResource, String generatedFileName) throws StorageException {
        File generatedFile = new File(getGeneratedFilesFolder(binaryResource), Tools.unAccent(generatedFileName));
        generatedFile.getParentFile().mkdirs();
        try {
            return new BufferedOutputStream(new FileOutputStream(generatedFile));
        } catch (java.io.FileNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public File copyFile(File source, BinaryResource pTargetBinaryResource) throws StorageException, FileNotFoundException {
        if (source.exists()) {
            File target = new File(getVirtualPath(pTargetBinaryResource));
            try {
                FileIO.copyFile(source, target);
                return target;
            } catch (IOException e) {
                throw new StorageException(new StringBuilder().append("Error copying ").append(source.getAbsolutePath()).append(" to ").append(pTargetBinaryResource.getFullName()).toString(), e);
            }
        } else {
            throw new FileNotFoundException(new StringBuilder("Can't find source file to copy ").append(source.getAbsolutePath()).toString());
        }
    }

    @Override
    public void delData(BinaryResource pBinaryResource) {
        File fileToRemove = new File(getVirtualPath(pBinaryResource));
        fileToRemove.delete();
        deleteGeneratedFiles(pBinaryResource);
        cleanRemove(fileToRemove.getParentFile());
    }

    @Override
    public String getExternalResourceURI(BinaryResource binaryResource) {
        return null;
    }

    @Override
    public String getShortenExternalResourceURI(BinaryResource binaryResource) {
        return null;
    }

    @Override
    public void deleteWorkspaceFolder(String workspaceId, List<BinaryResource> binaryResourcesInWorkspace) throws StorageException {
        if(workspaceId != null && !workspaceId.isEmpty()){
            try{
                File rootFolder = new File(new StringBuilder().append(vaultPath).append("/").append(workspaceId).toString());
                if(rootFolder.exists()){
                    FileUtils.deleteDirectory(rootFolder);
                }
            } catch (IOException e) {
                throw new StorageException(new StringBuilder().append("Error deleting directory for workspace ").append(workspaceId).toString(), e);
            }
        }
    }

    @Override
    public void renameData(File src, String pNewName) throws StorageException {
        if(src.exists()){
            src.renameTo(new File(new StringBuilder().append(src.getParentFile().getAbsolutePath()).append("/").append(Tools.unAccent(pNewName)).toString()));
        }else{
            throw new StorageException(new StringBuilder().append("Error renaming file ").append(src.getAbsolutePath()).toString());
        }
    }

    private String getVirtualPath(BinaryResource pBinaryResource) {
        String normalizedName = Tools.unAccent(pBinaryResource.getFullName());
        return new StringBuilder().append(this.vaultPath).append("/").append(normalizedName).toString();
    }

    private File getGeneratedFilesFolder(BinaryResource pBinaryResource) {
        File binaryResourceFile = new File(getVirtualPath(pBinaryResource));
        return new File(binaryResourceFile.getParentFile(), "_" + binaryResourceFile.getName());
    }

    private void cleanRemove(File pFile) {
        if(!pFile.equals(new File(vaultPath)) && pFile.delete())
            cleanRemove(pFile.getParentFile());
    }

    private InputStream getInputStream(File file) throws StorageException, FileNotFoundException {
        if (file.exists()) {
            try {
                return new BufferedInputStream(new FileInputStream(file));
            } catch (java.io.FileNotFoundException e) {
                throw new StorageException(e.getMessage(), e);
            }
        } else {
            throw new FileNotFoundException(new StringBuilder().append(file.getAbsolutePath()).append(" not found").toString());
        }
    }

    private void deleteGeneratedFiles(BinaryResource binaryResource) {
        File genFolder = getGeneratedFilesFolder(binaryResource);
        if (genFolder.exists()) {
            FileIO.rmDir(genFolder);
        }
    }
}
