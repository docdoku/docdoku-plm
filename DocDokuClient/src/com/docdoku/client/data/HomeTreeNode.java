package com.docdoku.client.data;

import com.docdoku.core.entities.Folder;

public class HomeTreeNode extends FolderTreeNode {

	private CheckedOutTreeNode mCheckedOutTreeNode;

	public HomeTreeNode(FolderTreeNode pParent) {
		super(Folder.createHomeFolder(MainModel.getInstance().getWorkspace().getId(),MainModel.getInstance().getLogin()), pParent);
		mCheckedOutTreeNode = new CheckedOutTreeNode(this);
	}

	public int folderSize() {
		return super.folderSize() + 1;
	}

	public FolderTreeNode getFolderChild(int pIndex) {
		if (pIndex == (folderSize() - 1))
			return mCheckedOutTreeNode;
		else
			return super.getFolderChild(pIndex);
	}
}