package com.docdoku.client.data;

import com.docdoku.core.entities.MasterDocument;

public class TagTreeNode extends FolderTreeNode {
    
    
    public TagTreeNode(String pCompletePath, FolderTreeNode pParent) {
        super(pCompletePath, pParent);
    }
    
    public FolderTreeNode getFolderChild(int pIndex) {
        return null;
    }
    
    public int getFolderIndexOfChild(Object pChild) {
        return -1;
    }
    
    public int folderSize() {
        return 0;
    }
    
    public Object getElementChild(int pIndex) {
        MasterDocument[] mdocs = MainModel.getInstance().findMDocsByTag(mFolder.getShortName());
        if(pIndex<mdocs.length)
            return mdocs[pIndex];
        else 
            return null;
    }
    
    public int elementSize() {
        MasterDocument[] mdocs = MainModel.getInstance().findMDocsByTag(
                mFolder.getShortName());
        return mdocs.length;
    }
}