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


public class TagRootTreeNode extends FolderTreeNode {

	public final static String TAG_ROOT_PATH=I18N.BUNDLE.getString("Tags_path_label");
	
    public TagRootTreeNode(FolderTreeNode pParent) {
        super(TAG_ROOT_PATH, pParent);
    }

    @Override
    public FolderTreeNode getFolderChild(int pIndex) {
        String[] tags = MainModel.getInstance().getTags();
        return new TagTreeNode(getCompletePath() + "/" + tags[pIndex],this);
    }

    @Override
    public int folderSize() {
        String[] tags = MainModel.getInstance().getTags();
        return tags.length;
    }

    @Override
    public Object getElementChild(int pIndex) {
        return null;
    }

    @Override
    public int elementSize(){
         return 0;
    }
}