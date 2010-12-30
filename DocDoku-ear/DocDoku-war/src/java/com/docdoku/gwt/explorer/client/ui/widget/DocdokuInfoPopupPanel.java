/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
