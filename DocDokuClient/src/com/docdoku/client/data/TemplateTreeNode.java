package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.MasterDocumentTemplate;

public class TemplateTreeNode extends FolderTreeNode {

	public final static String TEMPLATE_MODEL_PATH=I18N.BUNDLE.getString("Templates_path_label");
	
    public TemplateTreeNode(FolderTreeNode pParent) {
        super(TEMPLATE_MODEL_PATH, pParent);
    }

    @Override
    public FolderTreeNode getFolderChild(int pIndex) {
        return null;
    }

    @Override
    public int getFolderIndexOfChild(Object pChild) {
        return -1;
    }

    @Override
    public int folderSize() {
        return 0;
    }

    @Override
    public Object getElementChild(int pIndex) {
        MasterDocumentTemplate[] templates = MainModel.getInstance().getMDocTemplates();
        if(pIndex<templates.length)
            return templates[pIndex];
        else 
            return null;
    }

    @Override
    public int elementSize(){
        MasterDocumentTemplate[] templates = MainModel.getInstance().getMDocTemplates();
        return templates.length;
    }
}