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

package com.docdoku.client.ui.common;

import com.docdoku.client.data.ElementsTableModel;
import com.docdoku.core.document.DocumentMaster;

import javax.swing.*;

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
    
    public void selectElement(DocumentMaster pDocM){
        int index = mElementsTable.convertRowIndexToView(getModel().getIndexOfElement(pDocM));
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