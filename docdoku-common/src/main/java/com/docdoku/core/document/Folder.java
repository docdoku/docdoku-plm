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

package com.docdoku.core.document;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Stack;

/**
 * The {@link Folder} class is the unitary element of the tree structure.
 * Like in a regular file system, folder may contain other folders or documents.  
 * 
 * @author Florent Garin
 * @version 1.0, 02/06/08
 * @since   V1.0
 */
@Table(name="FOLDER")
@Entity
public class Folder implements Serializable, Comparable<Folder> {

    @Column(length=255)
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

    public Folder(String pParentFolderPath, String pShortName) {
        this(pParentFolderPath + "/" + pShortName);
    }

    public String getWorkspaceId(){
        return Folder.parseWorkspaceId(completePath);
    }
    
    public static String parseWorkspaceId(String pCompletePath){
        if(!pCompletePath.contains("/")) {
            return pCompletePath;
        }else{
            int index = pCompletePath.indexOf('/');
            return pCompletePath.substring(0, index);
        }
    }
    
    public boolean isRoot() {
        return !completePath.contains("/");
    }
    
    public boolean isHome() {
        try {
            int index = completePath.lastIndexOf('/');
            return completePath.charAt(index+1) == '~';
        } catch (IndexOutOfBoundsException pIOOBEx) {
            return false;
        }
    }
    
    public boolean isPrivate() {
        try {
            int index = completePath.indexOf('/');
            return completePath.charAt(index+1) == '~';
        } catch (IndexOutOfBoundsException pIOOBEx) {
            return false;
        }
    }
    
    public String getOwner() {
        String owner = null;
        if (isPrivate()) {
            int beginIndex = completePath.indexOf('/');
            int endIndex = completePath.indexOf("/", beginIndex+1);
            if(endIndex==-1) {
                endIndex = completePath.length();
            }
            
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
        Stack<Folder> foldersStack = new Stack<>();
        
        while (!(currentFolder == null)) {
            foldersStack.push(currentFolder);
            currentFolder = currentFolder.getParentFolder();
        }
        
        Folder[] folders = new Folder[foldersStack.size()];

        int i = 0;

        while(!foldersStack.empty()){
            folders[i++] = foldersStack.pop();
        }

        return folders;
    }
    
    public String getShortName() {
        if(isRoot()) {
            return completePath;
        }

        int index = completePath.lastIndexOf('/');
        return completePath.substring(index + 1);
    }

    public String getRoutePath() {
        int index = completePath.indexOf('/');

        if (index == -1) {
            return "";
        } else {
            return completePath.substring(index + 1).replaceAll("/", ":");
        }
    }

    public String getFoldersPath() {
        String path = getRoutePath();

        if (path != null && path.length() > 0) {
            return "folders/" + path;
        } else {
            return "folders";
        }
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
        if (!(pObj instanceof Folder)) {
            return false;
        }
        Folder folder = (Folder) pObj;
        return folder.completePath.equals(completePath);
    }
    
    @Override
    public int compareTo(Folder pFolder) {
        return completePath.compareTo(pFolder.completePath);
    }
}