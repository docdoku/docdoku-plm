package com.docdoku.client.data;

import com.docdoku.core.entities.WorkflowModel;


public class WorkflowModelTreeNode extends FolderTreeNode {

	public final static String WORKFLOW_MODEL_PATH="/*workflows";
	
    public WorkflowModelTreeNode(FolderTreeNode pParent) {
        super(WORKFLOW_MODEL_PATH, pParent);
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
        WorkflowModel[] workflows = MainModel.getInstance().getWorkflowModels();
        if(pIndex<workflows.length)
            return workflows[pIndex];
        else 
            return null;
    }

    public int elementSize(){
        WorkflowModel[] workflows = MainModel.getInstance().getWorkflowModels();
        return workflows.length;
    }
}