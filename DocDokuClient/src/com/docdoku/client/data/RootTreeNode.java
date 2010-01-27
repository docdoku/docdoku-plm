package com.docdoku.client.data;

import com.docdoku.core.entities.Folder;

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
    
    public int folderSize() {
        return super.folderSize() + 4;
    }
    
    public TagRootTreeNode getTagRootTreeNode(){
        return mTagRootTreeNode;
    }
    
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
    
    public String toString() {
        boolean numbered = MainModel.getInstance().getElementsTreeModel().getNumbered();
        String workspaceId = MainModel.getInstance().getWorkspace().getId();
        if (numbered)
            return getPosition() + "  " + workspaceId;
        else
            return workspaceId;
    }
    
    public String getPosition() {
        return 1 + "";
    }
}
