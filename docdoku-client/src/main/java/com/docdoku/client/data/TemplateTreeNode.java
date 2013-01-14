/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.DocumentMasterTemplate;

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
        DocumentMasterTemplate[] templates = MainModel.getInstance().getDocMTemplates();
        if(pIndex<templates.length)
            return templates[pIndex];
        else 
            return null;
    }

    @Override
    public int elementSize(){
        DocumentMasterTemplate[] templates = MainModel.getInstance().getDocMTemplates();
        return templates.length;
    }
}