/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.core.document;

import java.io.Serializable;
import java.util.Stack;
import javax.persistence.ManyToOne;

/**
 * The <a href="Folder.html">Folder</a>
 * class is the unitary element of the tree structure.
 * Like in a regular file system, folder may contain other folders or documents.  
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@javax.persistence.Entity
public class Folder implements Serializable, Comparable<Folder> {
       
    
    @javax.persistence.Id
    private String completePath="";
            
    @ManyToOne
    private Folder parentFolder;
    
    public Folder() {
    }
    
    public Folder(String pCompletePath) {
        completePath = pCompletePath;
        if (!isRoot() && !isHome()){
            int index = completePath.lastIndexOf('/');
            parentFolder = new Folder(completePath.substring(0, index));
        }
    }

    public void setCompletePath(String completePath) {
        this.completePath = completePath;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }
    

    public String getWorkspaceId(){
        return Folder.parseWorkspaceId(completePath);
    }
    
    public static String parseWorkspaceId(String pCompletePath){
        if(!pCompletePath.contains("/"))
            return pCompletePath;
        else{
            int index = pCompletePath.indexOf('/');
            return pCompletePath.substring(0, index);
        }
    }
    
    public Folder(String pParentFolderPath, String pShortName) {
        this(pParentFolderPath + "/" + pShortName);
    }
    
    public boolean isRoot() {
        return !completePath.contains("/");
    }
    
    public boolean isHome() {
        try {
            int index = completePath.lastIndexOf('/');
            return (completePath.charAt(index+1) == '~');
        } catch (IndexOutOfBoundsException pIOOBEx) {
            return false;
        }
    }
    
    public boolean isPrivate() {
        try {
            int index = completePath.indexOf('/');
            return (completePath.charAt(index+1) == '~');
        } catch (IndexOutOfBoundsException pIOOBEx) {
            return false;
        }
    }
    
    public String getOwner() {
        String owner = null;
        if (isPrivate()) {
            int beginIndex = completePath.indexOf('/');
            int endIndex = completePath.indexOf("/", beginIndex+1);
            if(endIndex==-1)
                endIndex=completePath.length();
            
            owner = completePath.substring(beginIndex+2, endIndex);
        }
        return owner;
    }
    
    @Override
    public String toString() {
        return completePath;
    }
    
    public String getCompletePath() {
        return completePath;
    }
    
    public Folder getParentFolder() {
        return parentFolder;
    }
    
    public Folder[] getAllFolders() {
        Folder currentFolder = this;
        Stack<Folder> foldersStack = new Stack<Folder>();
        
        while (!(currentFolder == null)) {
            foldersStack.push(currentFolder);
            currentFolder = currentFolder.getParentFolder();
        }
        
        Folder[] folders = new Folder[foldersStack.size()];
        for (int i = 0; !foldersStack.empty(); i++) {
            folders[i] = foldersStack.pop();
        }
        return folders;
    }

    public void changeName(String pNewName) {
        if (!isRoot() && !isHome()){
            int index = completePath.lastIndexOf('/');
            completePath = completePath.substring(0, index + 1) + pNewName;
        }
    }
    
    public String getShortName() {
        if(isRoot())
            return completePath;

        int index = completePath.lastIndexOf('/');
        return completePath.substring(index + 1);
    }
    
    
    public static Folder createRootFolder(String pWorkspaceId) {
        return new Folder(pWorkspaceId);
    }
    
    public static Folder createHomeFolder(String pWorkspaceId, String pLogin) {
        return new Folder(pWorkspaceId + "/~" + pLogin);
    }
    
    public Folder createSubFolder(String pShortName) {
        Folder subFolder = new Folder();
        subFolder.completePath=completePath + "/" + pShortName;
        subFolder.parentFolder=this;
        return subFolder;
    }
    
    
    @Override
    public int hashCode() {
        return completePath.hashCode();
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (!(pObj instanceof Folder))
            return false;
        Folder folder = (Folder) pObj;
        return folder.completePath.equals(completePath);
    }
    
    @Override
    public int compareTo(Folder pFolder) {
        return completePath.compareTo(pFolder.completePath);
    }
}