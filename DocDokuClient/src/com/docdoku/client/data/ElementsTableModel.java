package com.docdoku.client.data;


import javax.swing.table.TableModel;


public interface ElementsTableModel extends TableModel{

    public Object getElementAt(int pRowIndex);
    public int getIndexOfElement(Object pElement);
}
