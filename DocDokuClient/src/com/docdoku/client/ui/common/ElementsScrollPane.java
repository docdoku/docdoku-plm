package com.docdoku.client.ui.common;

import com.docdoku.client.data.ElementsTableModel;
import com.docdoku.core.entities.MasterDocument;
import java.awt.event.MouseListener;

import javax.swing.*;
import java.awt.*;

public class ElementsScrollPane extends JScrollPane {
    private ElementsTable mElementsTable;
    
    public ElementsScrollPane(ElementsTableModel pElementsTableModel, TransferHandler pTransferHandler) {
        mElementsTable = new ElementsTable(pElementsTableModel,pTransferHandler);
        createLayout();
    }
    
    private void createLayout() {
        getViewport().add(mElementsTable);
    }
    
    public ListSelectionModel getSelectionModel() {
        return mElementsTable.getSelectionModel();
    }
    
    
    public Object getSelectedElement() {
        return mElementsTable.getSelectedElement();
    }
    
    public Object[] getSelectedElements() {
        return mElementsTable.getSelectedElements();
    }
    
    public Object getElement(int pIndex) {
        return mElementsTable.getElement(pIndex);
    }
    
    public void unselectElement() {
        mElementsTable.clearSelection();
    }
    
    public void selectElement(MasterDocument pMDoc){
        int index = getModel().getIndexOfElement(pMDoc);
        if(index !=-1)
            getSelectionModel().setSelectionInterval(index,index);
    }
    
    public ElementsTableModel getModel() {
        return (ElementsTableModel) mElementsTable.getModel();
    }
    
    public ElementsTable getElementsTable(){
        return mElementsTable;
    }
    
}