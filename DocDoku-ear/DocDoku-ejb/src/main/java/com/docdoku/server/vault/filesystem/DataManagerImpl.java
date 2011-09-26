/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.
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
