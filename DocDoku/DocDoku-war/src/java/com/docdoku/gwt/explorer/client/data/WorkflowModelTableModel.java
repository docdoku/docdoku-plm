/*
 * WorkflowModelTableModel.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.client.ui.widget.table.TableModelIndex;
import com.docdoku.gwt.explorer.shared.WorkflowModelDTO;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class WorkflowModelTableModel implements TableModel {

    private String[] headers;
    private Object[][] data;
    private WorkflowModelDTO[] workflows;

    public WorkflowModelTableModel(WorkflowModelDTO[] workflows) {
        this.workflows = workflows;
        ExplorerI18NConstants explorerConstants=ServiceLocator.getInstance().getExplorerI18NConstants();
        this.headers = new String[]{ explorerConstants.tableID(), explorerConstants.tableAuthor(), explorerConstants.tableCreationDate()};
        this.data = new Object[workflows.length][3];
        for( int i=0; i < workflows.length ; i++ ){
            this.data[i][0] = workflows[i].getId();
            this.data[i][1] = workflows[i].getAuthor().getName();
            this.data[i][2] = workflows[i].getCreationDate();
        }
    }

    public int getRowCount() {
        return workflows.length ;
    }

    public int getColumnCount() {
        return 3 ;
    }

    public Object getValueAt(TableModelIndex index) {
        return data[index.getRow()][index.getColumn()] ;
    }

    public String[] getHeaderRow() {
        return headers ;
    }

    public String getEmptyCaseMessage() {
        return ServiceLocator.getInstance().getExplorerI18NConstants().emptyWorkflowModelLabel();
    }

    public String[] getTooltip(int row, int column) {
        return null ;
    }

    public WorkflowModelDTO getValueAt(int row){
        if (row >=0 && row < getRowCount()){
            return workflows[row];
        }else{
            return null ;
        }
    }

}
