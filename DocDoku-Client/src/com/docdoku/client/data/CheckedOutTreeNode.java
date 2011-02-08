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

import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.MasterDocument;

public class CheckedOutTreeNode extends FolderTreeNode {

    public final static String CHECKED_OUT_PATH = I18N.BUNDLE.getString("Checked_out_path_label");

    public CheckedOutTreeNode(FolderTreeNode pParent) {
        super(CHECKED_OUT_PATH, pParent);
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
        MasterDocument[] mdocs = MainModel.getInstance().getCheckedOutMDocs();
        if (pIndex < mdocs.length) {
            return mdocs[pIndex];
        } else {
            return null;
        }
    }

    @Override
    public int elementSize() {
        MasterDocument[] mdocs = MainModel.getInstance().getCheckedOutMDocs();
        return mdocs.length;
    }
}
