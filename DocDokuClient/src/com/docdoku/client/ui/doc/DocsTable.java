package com.docdoku.client.ui.doc;

import com.docdoku.client.data.DocsTableModel;
import com.docdoku.core.entities.Document;
import java.awt.Color;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

public class DocsTable extends JXTable {

    public DocsTable(TableModel pDocTableModel) {
        super(pDocTableModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setColumnControlVisible(true);
        Highlighter hl = HighlighterFactory.createAlternateStriping(UIManager.getColor("Panel.background"),Color.WHITE);
        setHighlighters(new Highlighter[]{hl});
    }

    public Document getSelectedDoc() {
        return ((DocsTableModel) getModel()).getDocAt(getSelectedRow());
    }

    @Override
    public TableCellRenderer getCellRenderer(int pRow, int pColumn) {
        return new DefaultCellRenderer();
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
}