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

package com.docdoku.gwt.explorer.client.ui.widget;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Emmanuel Nhan
 */
public class DocdokuInfoPopupPanel extends DecoratedPopupPanel{


    private Grid internalPanel ;

    public DocdokuInfoPopupPanel(int rows, int columns) {
        super(true) ;
        internalPanel = new Grid(rows, columns);
        super.setWidget(internalPanel);
    }

    public DocdokuInfoPopupPanel() {
        internalPanel = new Grid();
        super.setWidget(internalPanel);
    }



    public void setData(int row, int column, String content){
        if(row >= 0 && row < internalPanel.getRowCount() && column >=0 &&column< internalPanel.getColumnCount()){
            internalPanel.setText(row, column, content);
        }
    }

    public void setWidget(int row, int column, Widget w){
        if(row >= 0 && row < internalPanel.getRowCount() && column >=0 &&column< internalPanel.getColumnCount()){
            internalPanel.setWidget(row, column, w);
        }
    }

    public int insertRow(int beforeRow) {
        return internalPanel.insertRow(beforeRow);
    }

    public void removeRow(int row) {
        internalPanel.removeRow(row);
    }

    public int getRowCount() {
        return internalPanel.getRowCount();
    }

    public int getColumnCount() {
        return internalPanel.getColumnCount();
    }

    public boolean clearCell(int row, int column) {
        return internalPanel.clearCell(row, column);
    }

    
    
}
