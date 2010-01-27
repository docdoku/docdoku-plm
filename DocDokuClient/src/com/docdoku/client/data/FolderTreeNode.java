package com.docdoku.client.data;

import com.docdoku.core.entities.Folder;
import com.docdoku.core.entities.MasterDocument;

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

    public int compareTo(FolderTreeNode pFolderTreeNode) {
        return mFolder.compareTo(pFolderTreeNode.mFolder);
    }
}