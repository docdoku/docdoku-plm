package com.docdoku.client.ui.common;

import com.docdoku.client.data.ElementsTableModel;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.event.TableModelEvent;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class ElementsTable extends JXTable {
    
    public ElementsTable(ElementsTableModel pElementsTableModel,TransferHandler pTransferHandler) {
        super(pElementsTableModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDragEnabled(true);
        setTransferHandler(pTransferHandler);
        setColumnControlVisible(true);
        
        Highlighter hl = HighlighterFactory.createAlternateStriping(UIManager.getColor("Panel.background"),Color.WHITE);
        setHighlighters(new Highlighter[]{hl});
    }
    
    private void selectVisibleColumns(){
        TableColumnExt col = getColumnExt(I18N.BUNDLE.getString("LifeCycleState_column_label"));
        if(col!=null)
            col.setVisible(false);
        
        col = getColumnExt(I18N.BUNDLE.getString("CheckOutDate_column_label"));
        if(col!=null)
            col.setVisible(false);
        
        col = getColumnExt(I18N.BUNDLE.getString("CheckOutUser_column_label"));
        if(col!=null)
            col.setVisible(false);
        
        col = getColumnExt(I18N.BUNDLE.getString("CreationDate_column_label"));
        if(col!=null)
            col.setVisible(false);
        
    }
    
    @Override
    public void tableChanged(TableModelEvent e){
        super.tableChanged(e);
        selectVisibleColumns();
    }
    
    @Override
    public TableCellRenderer getCellRenderer(int pRow, int pColumn) {
        if (pColumn == 0)
            return new ElementCellRenderer();
        else
            return new DefaultCellRenderer();
    }
    
    public Object getElement(int pIndex){
        return ((ElementsTableModel) getModel()).getElementAt(convertRowIndexToModel(pIndex));
    }
    
    public Object getSelectedElement(){
        int row = getSelectedRow();
        if (row != -1){
            int convertedRow = convertRowIndexToModel(row);
            return ((ElementsTableModel) getModel()).getElementAt(convertedRow);
        }
        else return null;
    }
    public Object[] getSelectedElements(){
        int[] rows = getSelectedRows();
        
        Object[] selectedElements = new Object[rows.length];
        for (int i = 0; i < rows.length; i++) {
            selectedElements[i] = getElement(rows[i]);
        }
        return selectedElements;
    }

    private class DefaultCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable pTable,
                Object pValue,
                boolean pIsSelected,
                boolean pHasFocus,
                int pRow,
                int pColumn) {
            super.getTableCellRendererComponent(
                    pTable,
                    pValue,
                    pIsSelected,
                    pHasFocus,
                    pRow,
                    pColumn);

            if(pValue instanceof Date)
                setText(DateFormat.getInstance().format(pValue));
            return this;
        }
    }

    private class ElementCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable pTable,
                Object pValue,
                boolean pIsSelected,
                boolean pHasFocus,
                int pRow,
                int pColumn) {
            super.getTableCellRendererComponent(
                    pTable,
                    pValue,
                    pIsSelected,
                    pHasFocus,
                    pRow,
                    pColumn);
            Object element =
                    ((ElementsTableModel) pTable.getModel()).getElementAt(convertRowIndexToModel(pRow));
            Image img =
                    Toolkit.getDefaultToolkit().getImage(ElementsTable.class.getResource(
                    "/com/docdoku/client/resources/icons/document.png"));
            ImageIcon docIcon = new ImageIcon(img);
            img =
                    Toolkit.getDefaultToolkit().getImage(ElementsTable.class.getResource(
                    "/com/docdoku/client/resources/icons/document_edit.png"));
            ImageIcon checkedIcon = new ImageIcon(img);
            img =
                    Toolkit.getDefaultToolkit().getImage(ElementsTable.class.getResource(
                    "/com/docdoku/client/resources/icons/document_lock.png"));
            ImageIcon lockIcon = new ImageIcon(img);
            img =
                    Toolkit.getDefaultToolkit().getImage(ElementsTable.class.getResource(
                    "/com/docdoku/client/resources/icons/branch.png"));
            ImageIcon branchIcon = new ImageIcon(img);
            img =
                    Toolkit.getDefaultToolkit().getImage(ElementsTable.class.getResource(
                    "/com/docdoku/client/resources/icons/document_notebook.png"));
            ImageIcon templateIcon = new ImageIcon(img);



            if(element instanceof MasterDocument){
                MasterDocument mdoc = (MasterDocument) element;
                if (mdoc.isCheckedOut()) {
                    if (mdoc
                            .getCheckOutUser()
                            .equals(MainModel.getInstance().getUser()))
                        setIcon(checkedIcon);
                    else
                        setIcon(lockIcon);
                    DateFormat format = DateFormat.getInstance();
                    setToolTipText(
                            I18N.BUNDLE.getString("ElementsTable_toolTipText1")
                            + " "
                            + mdoc.getCheckOutUser().getName()
                            + " "
                            + I18N.BUNDLE.getString("ElementsTable_toolTipText2")
                            + " "
                            + format.format(mdoc.getCheckOutDate()));
                } else {
                    setIcon(docIcon);
                }
            }else if (element instanceof WorkflowModel){
                setIcon(branchIcon);
            }else if (element instanceof MasterDocumentTemplate){
                setIcon(templateIcon);
            }
            return this;
        }
    }
}