package com.docdoku.client.data;

import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.client.localization.I18N;


public class TagRootTreeNode extends FolderTreeNode {

	public final static String TAG_ROOT_PATH=I18N.BUNDLE.getString("Tags_path_label");
	
    public TagRootTreeNode(FolderTreeNode pParent) {
        super(TAG_ROOT_PATH, pParent);
    }

    public FolderTreeNode getFolderChild(int pIndex) {
        String[] tags = MainModel.getInstance().getTags();
        return new TagTreeNode(getCompletePath() + "/" + tags[pIndex],this);
    }

    public int folderSize() {
        String[] tags = MainModel.getInstance().getTags();
        return tags.length;
    }

    public Object getElementChild(int pIndex) {
        return null;
    }

    public int elementSize(){
         return 0;
    }
}