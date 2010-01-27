package com.docdoku.client.data;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.WorkflowModel;
import java.util.Date;

import javax.swing.table.*;

public class FolderBasedElementsTableModel extends AbstractTableModel implements ElementsTableModel {
    
    private final static String MDOC_COLUMN_NAME[] =
    {
        I18N.BUNDLE.getString("Id_column_label"),
        I18N.BUNDLE.getString("Version_column_label"),
        I18N.BUNDLE.getString("LastIteration_column_label"),
        I18N.BUNDLE.getString("Type_column_label"),
        I18N.BUNDLE.getString("Title_column_label"),
        I18N.BUNDLE.getString("Author_column_label"),
        I18N.BUNDLE.getString("ModificationDate_column_label"),
        I18N.BUNDLE.getString("CreationDate_column_label"),
        I18N.BUNDLE.getString("CheckOutUser_column_label"),
        I18N.BUNDLE.getString("CheckOutDate_column_label"),
        I18N.BUNDLE.getString("LifeCycleState_column_label")
    };
    
    private final static String WORKFLOW_COLUMN_NAME[] =
    {
        I18N.BUNDLE.getString("Id_column_label"),
        I18N.BUNDLE.getString("Author_column_label"),
        I18N.BUNDLE.getString("CreationDate_column_label")
    };
    
    private final static String TEMPLATE_COLUMN_NAME[] =
    {
        I18N.BUNDLE.getString("Id_column_label"),
        I18N.BUNDLE.getString("DocumentType_column_label"),
        I18N.BUNDLE.getString("Author_column_label"),
        I18N.BUNDLE.getString("CreationDate_column_label")
    };
    
    private FolderTreeNode mFolder;
    private String[] mColumnName;
    
    
    
    public FolderBasedElementsTableModel() {
        mColumnName=MDOC_COLUMN_NAME;
    }
    
    @Override
    public Class getColumnClass(int column) {
        Class returnValue;
        if ((column >= 0) && (column < getColumnCount()) && getRowCount()>0) {
            returnValue = getValueAt(0, column).getClass();
        } else {
            returnValue = Object.class;
        }
        return returnValue;
    }
    
    public int getColumnCount() {
        return mColumnName.length;
    }
    
    @Override
    public String getColumnName(int pColumnIndex) {
        return mColumnName[pColumnIndex];
    }
    
    public int getRowCount() {
        return ((mFolder == null) ? 0 : mFolder.elementSize());
    }
    
    public Object getValueAt(int pRowIndex, int pColumnIndex) {
        Object element = mFolder.getElementChild(pRowIndex);       
        if (element instanceof MasterDocument) {
            MasterDocument mdoc = (MasterDocument)element;
            Document doc = mdoc.getLastIteration();
            switch (pColumnIndex) {
                case 0 :
                    return mdoc.getId();
                case 1 :
                    return mdoc.getVersion();
                case 2 :
                    return new Integer((doc == null) ? 0 : doc.getIteration());
                case 3 :
                    String type = mdoc.getType();
                    return type==null?"":type;
                case 4 :
                    String title=mdoc.getTitle();
                    return title==null?"":title;
                case 5 :
                    return mdoc.getAuthor().getName();
                case 6 :
                    return doc == null ? "":doc.getCreationDate();
                case 7 :
                    return mdoc.getCreationDate();
                case 8 :
                    User checkOutUser = mdoc.getCheckOutUser();
                    return checkOutUser==null?"":checkOutUser.getName();
                case 9 :
                    Date checkOutDate = mdoc.getCheckOutDate();
                    return checkOutDate==null?"":checkOutDate;
                case 10 :
                    String lc = mdoc.getLifeCycleState();
                    return lc==null?"":lc;
            }
        } else if (element instanceof WorkflowModel) {
            WorkflowModel wfModel=(WorkflowModel)element;
            switch (pColumnIndex) {
                case 0 :
                    return wfModel.getId();
                case 1 :
                    return wfModel.getAuthor().getName();
                case 2 :
                    return wfModel.getCreationDate();
            }
            
        }else if (element instanceof MasterDocumentTemplate) {
            MasterDocumentTemplate template=(MasterDocumentTemplate)element;
            switch (pColumnIndex) {
                case 0 :
                    return template.getId();
                case 1 :
                    String documentType=template.getDocumentType();
                    return documentType==null?"":documentType;
                case 2 :
                    return template.getAuthor().getName();
                case 3 :
                    return template.getCreationDate();
            }
            
        }
        return null;
    }
    
    public Object getElementAt(int pRowIndex) {
        return mFolder.getElementChild(pRowIndex);
    }
    
    public String getFolderCompletePath() {
        return mFolder==null?null:mFolder.getCompletePath();
    }
    
    public int getIndexOfElement(Object pElement) {
        int count = getRowCount();
        for (int i = 0; i < count; i++)
            if (getElementAt(i).equals(pElement))
                return i;
        return -1;
    }
    
    
    public void setFolder(FolderTreeNode pFolder) {
        String[] newColumnName;
        if(pFolder instanceof WorkflowModelTreeNode)
            newColumnName=WORKFLOW_COLUMN_NAME;
        else if(pFolder instanceof TemplateTreeNode)
            newColumnName=TEMPLATE_COLUMN_NAME;
        else
            newColumnName=MDOC_COLUMN_NAME;

        mFolder = pFolder;
        if(!mColumnName.equals(newColumnName)){
            mColumnName=newColumnName;
            fireTableStructureChanged();
        }
        
        fireTableDataChanged();
    }
}
