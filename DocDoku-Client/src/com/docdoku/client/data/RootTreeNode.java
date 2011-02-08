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

public class RootTreeNode extends FolderTreeNode {
    
    private HomeTreeNode mHomeTreeNode;
    private WorkflowModelTreeNode mWorkflowModelTreeNode;
    private TemplateTreeNode mTemplateTreeNode;
    private TagRootTreeNode mTagRootTreeNode;
    
    public RootTreeNode() {
        super(Folder.createRootFolder(MainModel.getInstance().getWorkspace().getId()), null);
        mHomeTreeNode = new HomeTreeNode(this);
        mWorkflowModelTreeNode = new WorkflowModelTreeNode(this);
        mTemplateTreeNode = new TemplateTreeNode(this);
        mTagRootTreeNode = new TagRootTreeNode(this);
    }
    
    @Override
    public int folderSize() {
        return super.folderSize() + 4;
    }
    
    public TagRootTreeNode getTagRootTreeNode(){
        return mTagRootTreeNode;
    }
    
    @Override
    public FolderTreeNode getFolderChild(int pIndex) {
        switch (pIndex) {
            case 0:
                return mHomeTreeNode;
            case 1:
                return mWorkflowModelTreeNode;
            case 2:
                return mTemplateTreeNode;
            case 3:
                return mTagRootTreeNode;
            default:
                return super.getFolderChild(pIndex - 4);
        }
    }
    
    @Override
    public String toString() {
        boolean numbered = MainModel.getInstance().getElementsTreeModel().getNumbered();
        String workspaceId = MainModel.getInstance().getWorkspace().getId();
        if (numbered)
            return getPosition() + "  " + workspaceId;
        else
            return workspaceId;
    }
    
    @Override
    public String getPosition() {
        return 1 + "";
    }
}
