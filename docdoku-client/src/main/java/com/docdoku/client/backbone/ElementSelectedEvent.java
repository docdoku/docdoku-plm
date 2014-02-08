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

package com.docdoku.client.backbone;

import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.workflow.WorkflowModel;

import java.util.EventObject;

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
