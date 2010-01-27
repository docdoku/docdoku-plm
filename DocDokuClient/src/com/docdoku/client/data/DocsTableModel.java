package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.MasterDocument;
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
