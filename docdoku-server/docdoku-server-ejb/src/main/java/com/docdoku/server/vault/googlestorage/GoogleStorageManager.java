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

package com.docdoku.server.vault.googlestorage;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.services.VaultException;
import com.docdoku.core.util.Tools;
import com.docdoku.server.vault.DataManager;


import java.io.*;

/**
 * @author Asmae Chadid
 */
public class GoogleStorageManager implements DataManager {

    public GoogleStorageManager() {
    }

    @Override
    public void delData(BinaryResource pBinaryResource) {

        GoogleStorageCloud gStorage = new GoogleStorageCloud();
        try {
            gStorage.delete(pBinaryResource.getFullName());
        } catch (IOException e) {
            throw new VaultException(new FileNotFoundException(pBinaryResource.getFullName()));
        }
    }

    @Override
    public void copyData(BinaryResource pSourceBinaryResource, BinaryResource pTargetBinaryResource) {
        try {
            GoogleStorageCloud gStorage = new GoogleStorageCloud();
            gStorage.upload(new FileInputStream(getDataFile(pSourceBinaryResource)),pTargetBinaryResource.getFullName());
        } catch (IOException pEx) {
           throw new VaultException(new FileNotFoundException(pEx.getMessage()));
        }
    }

    @Override
    public File getDataFile(BinaryResource pBinaryResource) {

        GoogleStorageCloud gStorage = new GoogleStorageCloud();

        try {
            File realFile = gStorage.getFile(pBinaryResource.getFullName());
            return realFile;
        } catch (IOException e) {
            throw new VaultException(new FileNotFoundException(pBinaryResource.getFullName()));
        }
    }

    @Override
    public File getVaultFile(BinaryResource pBinaryResource) {
        String normalizedName = Tools.unAccent(pBinaryResource.getFullName());
        return new File(normalizedName);
    }

    @Override
    public long writeFile(File vaultFile, InputStream in) {
        GoogleStorageCloud gStorage = new GoogleStorageCloud();
        try {
            return gStorage.upload(in, vaultFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
