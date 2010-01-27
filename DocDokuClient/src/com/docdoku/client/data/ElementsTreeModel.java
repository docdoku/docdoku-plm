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

import com.docdoku.core.entities.Folder;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;

public class ElementsTreeModel implements TreeModel {
    private List<TreeModelListener> mTreeModelListeners;
    
    private RootTreeNode mRoot;
    
    public ElementsTreeModel() {
        mTreeModelListeners = new ArrayList<TreeModelListener>();
        mRoot = new RootTreeNode();
    }
    
    public void addTreeModelListener(TreeModelListener pTreeModelListener) {
        mTreeModelListeners.add(pTreeModelListener);
    }
    
    public void removeTreeModelListener(TreeModelListener pTreeModelListener) {
        mTreeModelListeners.remove(pTreeModelListener);
    }
    
    public void valueForPathChanged(TreePath pPath, Object pNewValue) {
    }
    
    public RootTreeNode getRoot() {
        return mRoot;
    }
    
    public Object getChild(Object pParent, int pIndex) {
        return ((FolderTreeNode) pParent).getFolderChild(pIndex);
    }
    
    public int getChildCount(Object pParent) {
        return ((FolderTreeNode) pParent).folderSize();
    }
    
    public int getIndexOfChild(Object pParent, Object pChild) {
        if (pParent == null || pChild == null)
            return -1;
        else
            return ((FolderTreeNode) pParent).getFolderIndexOfChild(pChild);
    }
    
    public boolean isLeaf(Object pNode) {
        return false;
    }
    
    public void setNumbered(boolean numberedNode){
        Prefs.setNumbered(numberedNode);
        fireAllTreeNodesChanged();
    }
    
    public boolean getNumbered(){
        return Prefs.getNumbered();
    }
    
    public FolderTreeNode[] getPath(String pCompletePath) {
        Folder[] allFolders = new Folder(pCompletePath).getAllFolders();
        FolderTreeNode[] path;
        if(allFolders[0].isHome()){
            path = new FolderTreeNode[allFolders.length+1];
            path[0] = getRoot();
            path[1] = new HomeTreeNode(path[0]);
            for (int i=1; i < allFolders.length; i++) {
                path[i+1] = new FolderTreeNode(allFolders[i].toString(),
                    path[i]);
            }
        }else{
            path = new FolderTreeNode[allFolders.length];
            path[0] = getRoot();
            for (int i=1; i < allFolders.length; i++) {
                path[i] = new FolderTreeNode(allFolders[i].toString(),
                    path[i - 1]);
            }
        }
        return path;
    }
    
    protected void fireAllTreeNodesChanged() {
        TreePath rootPath = new TreePath(mRoot);
        for (int i = 0; i < mTreeModelListeners.size(); i++) {
            TreeModelListener listener = mTreeModelListeners
                    .get(i);
            listener.treeNodesChanged(new TreeModelEvent(this, rootPath));
        }
    }
    
    protected void fireAllTreeStructureChanged() {
        fireTreeStructureChanged(mRoot);
    }
    
    protected void fireTagTreeStructureChanged() {
        fireTreeStructureChanged(mRoot,mRoot.getTagRootTreeNode());
    }
    
    protected void fireTreeStructureChanged(FolderTreeNode... pNodes) {
        TreePath path = new TreePath(pNodes);
        for (int i = 0; i < mTreeModelListeners.size(); i++) {
            TreeModelListener listener = mTreeModelListeners
                    .get(i);
            listener.treeStructureChanged(new TreeModelEvent(this, path));
        }
    }
    
    protected void fireTreeNodesInserted(Object[] pPath, int[] pChildIndices,
            Object[] pChildren) {
        TreeModelEvent event = new TreeModelEvent(this, pPath, pChildIndices,
                pChildren);
        for (int i = 0; i < mTreeModelListeners.size(); i++) {
            TreeModelListener listener = mTreeModelListeners
                    .get(i);
            listener.treeNodesInserted(event);
        }
    }
    
    protected void fireTreeNodesRemoved(Object[] pPath, int[] pChildIndices,
            Object[] pChildren) {
        TreeModelEvent event = new TreeModelEvent(this, pPath, pChildIndices,
                pChildren);
        for (int i = 0; i < mTreeModelListeners.size(); i++) {
            TreeModelListener listener = mTreeModelListeners
                    .get(i);
            listener.treeNodesRemoved(event);
        }
    }
    
}