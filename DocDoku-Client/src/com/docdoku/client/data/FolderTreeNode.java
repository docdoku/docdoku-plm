/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.client.data;

import com.docdoku.core.document.Folder;
import com.docdoku.core.document.MasterDocument;

public class FolderTreeNode implements Comparable<FolderTreeNode> {

    protected Folder mFolder;

    protected FolderTreeNode mParent;

    public FolderTreeNode(String pCompletePath, FolderTreeNode pParent) {
        this(new Folder(pCompletePath), pParent);
    }

    public FolderTreeNode(Folder pFolder, FolderTreeNode pParent) {
        mFolder = pFolder;
        mParent = pParent;
    }

    public FolderTreeNode getFolderChild(int pIndex) {
        FolderTreeNode[] folderTreeNodes = MainModel.getInstance()
                .getFolderTreeNodes(this);
        return folderTreeNodes[pIndex];
    }

    public Object getElementChild(int pIndex) {
        MasterDocument[] mdocs = MainModel.getInstance().findMDocsByFolder(mFolder.getCompletePath());
        if(pIndex<mdocs.length)
            return mdocs[pIndex];
        else 
            return null;
    }

    public int getFolderIndexOfChild(Object pChild) {
        for (int i = 0; i < folderSize(); i++)
            if (getFolderChild(i).equals(pChild))
                return i;
        return -1;
    }

    public int folderSize() {
        FolderTreeNode[] folderTreeNodes = MainModel.getInstance()
                .getFolderTreeNodes(this);
        return folderTreeNodes.length;
    }

    public int elementSize() {
        MasterDocument[] mdocs = MainModel.getInstance().findMDocsByFolder(
                mFolder.getCompletePath());
        return mdocs.length;
    }

    public String getPosition() {
        return mParent.getPosition() + "."
                + (mParent.getFolderIndexOfChild(this) + 1);
    }

    @Override
    public String toString() {
        boolean numbered = MainModel.getInstance().getElementsTreeModel()
                .getNumbered();
        if (numbered)
            return getPosition() + "  " + mFolder.getShortName();
        else
            return mFolder.getShortName();
    }

    public String getName(){
        return mFolder.getShortName();
    }
    
    public String getCompletePath() {
        return mFolder.getCompletePath();
    }
    
    public Folder getFolder() {
        return mFolder;
    }

    @Override
    public boolean equals(Object pObj) {
        if (!(pObj instanceof FolderTreeNode))
            return false;
        FolderTreeNode treeNode = (FolderTreeNode) pObj;
        return treeNode.mFolder.equals(mFolder);
    }

    @Override
    public int hashCode() {
        return mFolder.hashCode();
    }

    @Override
    public int compareTo(FolderTreeNode pFolderTreeNode) {
        return mFolder.compareTo(pFolderTreeNode.mFolder);
    }
}