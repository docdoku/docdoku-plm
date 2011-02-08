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

package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.Document;
import com.docdoku.core.document.MasterDocument;
import javax.swing.table.*;

public class DocsTableModel extends AbstractTableModel {

    private final static String COLUMNN_NAME[] = {I18N.BUNDLE.getString("Iteration_column_label"), I18N.BUNDLE.getString("Author_column_label"), I18N.BUNDLE.getString("RevisionNote_column_label"), I18N.BUNDLE.getString("ModificationDate_column_label")};
    private MasterDocument mMDoc;

    public DocsTableModel(MasterDocument pMDoc) {
        mMDoc = pMDoc;
    }

    public int getColumnCount() {
        return COLUMNN_NAME.length;
    }

    @Override
    public String getColumnName(int pColumnIndex) {
        return COLUMNN_NAME[pColumnIndex];
    }

    public int getRowCount() {
        return mMDoc.getNumberOfIterations();
    }

    public Document getDocAt(int pRowIndex) {
        return mMDoc.getIteration(pRowIndex + 1);
    }

    public Object getValueAt(int pRowIndex, int pColumnIndex) {
        Document doc = getDocAt(pRowIndex);
        switch (pColumnIndex) {
            case 0:
                return new Integer(doc.getIteration());
            case 1:
                return doc.getAuthor().getName();
            case 2:
                return doc.getRevisionNote();
            case 3:
                return doc.getCreationDate();
        }
        return null;
    }

    
}
