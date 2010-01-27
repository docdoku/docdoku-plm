package com.docdoku.client.backbone;

import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.core.entities.MasterDocumentTemplate;
import java.util.EventObject;

import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.WorkflowModel;

public class ElementSelectedEvent extends EventObject {

	private Object mElement;
	private ElementType mType;
	
	public enum ElementType{MasterDocument,MasterDocumentTemplate,WorkflowModel,FolderTreeNode};
	
	public ElementSelectedEvent(Object pSource, MasterDocument pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.MasterDocument;
	}
        
        public ElementSelectedEvent(Object pSource, MasterDocumentTemplate pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.MasterDocumentTemplate;
	}
	
	public ElementSelectedEvent(Object pSource, FolderTreeNode pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.FolderTreeNode;
	}
	
	public ElementSelectedEvent(Object pSource, WorkflowModel pElement) {
		super(pSource);
		mElement=pElement;
		mType=ElementType.WorkflowModel;
	}

	public Object getElement() {
		return mElement;
	}
	
	public ElementType getElementType(){
		return mType;
	}

}
