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

package com.docdoku.client.ui.doc;

import com.docdoku.client.data.DocsTableModel;
import com.docdoku.core.document.DocumentIteration;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.text.DateFormat;
import java.util.Date;

public class DocsTable extends JXTable {

    public DocsTable(TableModel pDocTableModel) {
        super(pDocTableModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setColumnControlVisible(true);
        Highlighter hl = HighlighterFactory.createAlternateStriping(UIManager.getColor("Panel.background"),Color.WHITE);
        setHighlighters(new Highlighter[]{hl});
    }

    public DocumentIteration getSelectedDoc() {
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
                setText(DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT).format(pValue));
            return this;
        }
    }
}