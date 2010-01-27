package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.Workflow;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.table.*;

public class SearchMDocsResultTableModel extends AbstractTableModel implements ElementsTableModel {

    private final static String COLUMN_NAME[] =
            {
        I18N.BUNDLE.getString("Id_column_label"),
        I18N.BUNDLE.getString("Version_column_label"),
        I18N.BUNDLE.getString("Type_column_label"),
        I18N.BUNDLE.getString("Title_column_label"),
        I18N.BUNDLE.getString("Author_column_label"),        
        I18N.BUNDLE.getString("ModificationDate_column_label"),
        I18N.BUNDLE.getString("CreationDate_column_label"),
        I18N.BUNDLE.getString("Location_column_label"),
        I18N.BUNDLE.getString("CheckOutUser_column_label"),
        I18N.BUNDLE.getString("CheckOutDate_column_label"),
        I18N.BUNDLE.getString("LifeCycleState_column_label")
    };
    private MasterDocument[] mMDocs;

    public SearchMDocsResultTableModel(MasterDocument[] pMDocs) {
        mMDocs = pMDocs;
    }

    public int getColumnCount() {
        return COLUMN_NAME.length;
    }

    public String getColumnName(int pColumnIndex) {
        return COLUMN_NAME[pColumnIndex];
    }

    public int getRowCount() {
        return mMDocs.length;
    }

    public Class getColumnClass(int column) {
        Class returnValue;
        if ((column >= 0) && (column < getColumnCount()) && getRowCount() > 0) {
            returnValue = getValueAt(0, column).getClass();
        } else {
            returnValue = Object.class;
        }
        return returnValue;
    }

    public Object getValueAt(int pRowIndex, int pColumnIndex) {
        MasterDocument mdoc = mMDocs[pRowIndex];
        Document doc = mdoc.getLastIteration();
        switch (pColumnIndex) {
            case 0:
                return mdoc.getId();
            case 1:
                return mdoc.getVersion();
            case 2:
                String type = mdoc.getType();
                return type == null ? "" : type;
            case 3:
                String title = mdoc.getTitle();
                return title == null ? "" : title;
            case 4:
                return mdoc.getAuthor().getName();                
            case 5:
                return doc == null ? "" : doc.getCreationDate();
            case 6:
                return mdoc.getCreationDate();
            case 7:
                return mdoc.getLocation();
            case 8:
                User checkOutUser = mdoc.getCheckOutUser();
                return checkOutUser == null ? "" : checkOutUser.getName();
            case 9:
                Date checkOutDate = mdoc.getCheckOutDate();
                return checkOutDate == null ? "" : checkOutDate;
            case 10:
                String lc = mdoc.getLifeCycleState();
                return lc == null ? "" : lc;
        }
        return null;
    }

    public MasterDocument getElementAt(int pRowIndex) {
        return mMDocs[pRowIndex];
    }

    public int getIndexOfElement(Object pElement) {
        int count = getRowCount();
        for (int i = 0; i < count; i++) {
            if (getElementAt(i).equals(pElement)) {
                return i;
            }
        }
        return -1;
    }
}
