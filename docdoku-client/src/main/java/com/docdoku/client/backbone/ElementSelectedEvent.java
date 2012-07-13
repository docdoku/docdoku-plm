/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.client.backbone;

import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.core.document.DocumentMasterTemplate;
import java.util.EventObject;

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.WorkflowModel;

public class ElementSelectedEvent extends EventObject {

	private Object mElement;
	private ElementType mType;
	
	public enum ElementType{MASTER_DOCUMENT,MASTER_DOCUMENT_TEMPLATE,WORKFLOW_MODEL,FOLDER_TREE_NODE};
	
	public ElementSelectedEvent(Object pSource, DocumentMaster pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.MASTER_DOCUMENT;
	}
        
        public ElementSelectedEvent(Object pSource, DocumentMasterTemplate pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.MASTER_DOCUMENT_TEMPLATE;
	}
	
	public ElementSelectedEvent(Object pSource, FolderTreeNode pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.FOLDER_TREE_NODE;
	}
	
	public ElementSelectedEvent(Object pSource, WorkflowModel pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.WORKFLOW_MODEL;
	}

	public Object getElement() {
		return mElement;
	}
	
	public ElementType getElementType(){
		return mType;
	}

}
