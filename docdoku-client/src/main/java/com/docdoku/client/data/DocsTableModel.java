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

package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMaster;
import javax.swing.table.*;

public class DocsTableModel extends AbstractTableModel {

    private final static String COLUMNN_NAME[] = {I18N.BUNDLE.getString("Iteration_column_label"), I18N.BUNDLE.getString("Author_column_label"), I18N.BUNDLE.getString("RevisionNote_column_label"), I18N.BUNDLE.getString("ModificationDate_column_label")};
    private DocumentMaster mDocM;

    public DocsTableModel(DocumentMaster pDocM) {
        mDocM = pDocM;
    }

    public int getColumnCount() {
        return COLUMNN_NAME.length;
    }

    @Override
    public String getColumnName(int pColumnIndex) {
        return COLUMNN_NAME[pColumnIndex];
    }

    public int getRowCount() {
        return mDocM.getNumberOfIterations();
    }

    public DocumentIteration getDocAt(int pRowIndex) {
        return mDocM.getIteration(pRowIndex + 1);
    }

    public Object getValueAt(int pRowIndex, int pColumnIndex) {
        DocumentIteration doc = getDocAt(pRowIndex);
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
