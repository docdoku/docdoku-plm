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

package com.docdoku.server.vault.filesystem;

import com.docdoku.core.services.VaultException;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.util.Tools;
import com.docdoku.server.vault.DataManager;

import java.io.*;


public class DataManagerImpl implements DataManager {
    
    private final File mBaseDir;
    private final static int BUFFER_CAPACITY = 1024 * 16;
    private final static int CHUNK_SIZE = 1024 * 8;
    
    public DataManagerImpl(File pBaseDir) {
        mBaseDir = pBaseDir;
    }
    
    @Override
    public void delData(BinaryResource pBinaryResource) {
        File fileToRemove=getVaultFile(pBinaryResource);
        String woExName=FileIO.getFileNameWithoutExtension(fileToRemove);
        fileToRemove.delete();
        new File(fileToRemove.getParentFile(),woExName+".swf").delete();
        new File(fileToRemove.getParentFile(),woExName+".pdf").delete();
        cleanRemove(fileToRemove.getParentFile());
    }
    
        
    @Override
    public void copyData(BinaryResource pSourceBinaryResource, BinaryResource pTargetBinaryResource) {
        try {
            FileIO.copyFile(getDataFile(pSourceBinaryResource), getVaultFile(pTargetBinaryResource));
        } catch (Exception pEx) {
            throw new VaultException(pEx);
        }
    }
        
    
    @Override
    public File getVaultFile(BinaryResource pBinaryResource) {
        String normalizedName = Tools.unAccent(pBinaryResource.getFullName());
        return new File(mBaseDir,normalizedName);
    }

    @Override
    public long writeFile(File vaultFile, InputStream in) throws Exception {
        vaultFile.getParentFile().mkdirs();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(vaultFile), BUFFER_CAPACITY);
        out = new BufferedOutputStream(out, BUFFER_CAPACITY);

        byte[] data = new byte[CHUNK_SIZE];
        int length;
        try {
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }
        } finally {
            in.close();
            out.flush();
            out.close();
        }
        return vaultFile.length();
    }

    @Override
    public File getDataFile(BinaryResource pBinaryResource){
        String normalizedName = Tools.unAccent(pBinaryResource.getFullName());
        File realFile = new File(mBaseDir,normalizedName);
        if(realFile.exists())
            return realFile;
        else{
            BinaryResource previous = pBinaryResource.getPrevious();
            if(previous!=null)
                return getDataFile(previous);
            else{
                throw new VaultException(new FileNotFoundException(realFile.toString()));
            }
        }
    }
    
    private void cleanRemove(File pFile){
        if(!pFile.equals(mBaseDir) && pFile.delete())
            cleanRemove(pFile.getParentFile());
    }
    
}
