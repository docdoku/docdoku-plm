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

package com.docdoku.client.ui.common;

import com.docdoku.client.data.ElementsTableModel;
import com.docdoku.core.entities.MasterDocument;

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
    
    public void selectElement(MasterDocument pMDoc){
        int index = mElementsTable.convertRowIndexToView(getModel().getIndexOfElement(pMDoc));
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