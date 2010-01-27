package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.MasterDocument;

public class CheckedOutTreeNode extends FolderTreeNode {

	public final static String CHECKED_OUT_PATH=I18N.BUNDLE.getString("Checked_out_path_label");
	
	public CheckedOutTreeNode(FolderTreeNode pParent) {
		super(CHECKED_OUT_PATH, pParent);
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
            MasterDocument[] mdocs = MainModel.getInstance().getCheckedOutMDocs();
            if(pIndex<mdocs.length)
                return mdocs[pIndex];
            else 
            return null;
	}

	public int elementSize() {
		MasterDocument[] mdocs = MainModel.getInstance().getCheckedOutMDocs();
		return mdocs.length;
	}
}