/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.client.ui.widget.table.TableModelIndex;
import com.docdoku.gwt.explorer.shared.MasterDocumentTemplateDTO;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class MDocTemplateTableModel implements TableModel{

    private String[] headers;
    private Object[][] data;
    private MasterDocumentTemplateDTO[] templates;

    public MDocTemplateTableModel(MasterDocumentTemplateDTO[] templates) {
        this.templates = templates;

        // headers ;
        ExplorerI18NConstants explorerConstants=ServiceLocator.getInstance().getExplorerI18NConstants();
        this.headers = new String[]{ explorerConstants.tableID(),explorerConstants.tableDocumentType(), explorerConstants.tableAuthor(), explorerConstants.tableCreationDate()};
        // data
        this.data = new Object[templates.length][4];
        for( int i=0; i < templates.length ; i++ ){
            this.data[i][0] = templates[i].getId();
            this.data[i][1] = templates[i].getDocumentType();
            this.data[i][2] = templates[i].getAuthor().getName();
            this.data[i][3] = templates[i].getCreationDate();
        }
    }

    

    public int getRowCount() {
        return templates.length;
    }

    public int getColumnCount() {
        return 4 ;
    }

    public Object getValueAt(TableModelIndex index) {
        return data[index.getRow()][index.getColumn()] ;
    }

    public String[] getHeaderRow() {
        return headers;
    }

    public String getEmptyCaseMessage() {
        return ServiceLocator.getInstance().getExplorerI18NConstants().emptyDocTemplateLabel();
    }

    public String[] getTooltip(int row, int column) {
        return null ;
    }

    public MasterDocumentTemplateDTO getValueAt(int row){
      if (row >=0 && row < getRowCount()){
            return templates[row];
        }else{
            return null ;
        }
    }
    
}
