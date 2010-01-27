package com.docdoku.client.actions;

import com.docdoku.client.data.ElementsTableModel;
import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ElementsTable;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MDocSelection;
import com.docdoku.core.entities.MasterDocument;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;


public class MDocTransferHandler extends TransferHandler {
    
    private DataFlavor mMDocFlavor;
    
    public MDocTransferHandler() {
        try {
            mMDocFlavor = new DataFlavor(GUIConstants.MDOC_FLAVOR);
        } catch (ClassNotFoundException pCNFEx) {
            throw new RuntimeException("Unexpected error: unrecognized data flavor", pCNFEx);
        }
    }
    
    public boolean canImport(JComponent pComp,
            DataFlavor[] pTransferFlavors) {
        
        if (pComp instanceof JTree) {
            for (DataFlavor flavor : pTransferFlavors) {
                if (flavor.equals(mMDocFlavor))
                    return true;
            }
        } else {
            for (DataFlavor flavor : pTransferFlavors) {
                if (flavor.equals(DataFlavor.javaFileListFlavor))
                    return true;
            }
        }
        
        return false;
    }
    
    public int getSourceActions
            (JComponent
            pComp) {
        if (pComp instanceof JTable)
            return TransferHandler.MOVE;
        else
            return TransferHandler.NONE;
    }
    
    public boolean importData
            (JComponent
            pComp, Transferable
            pTransferable) {
        try {
            if (pComp instanceof JTree) {
                MasterDocument mdoc = (MasterDocument) pTransferable.getTransferData(mMDocFlavor);
                JTree tree = (JTree) pComp;
                tree.getSelectionPath();
                String path = ((FolderTreeNode) tree.getLastSelectedPathComponent()).getCompletePath();
                MainController.getInstance().moveMDoc(path, mdoc);
                return true;
            } else {
                
            }
            
        } catch (Exception pEx) {
            String message = pEx.getMessage()==null?I18N.BUNDLE
                    .getString("Error_unknown"):pEx.getMessage();
            JOptionPane.showMessageDialog(null,
                    message, I18N.BUNDLE
                    .getString("Error_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        
        
        return false;
    }
    
    protected Transferable createTransferable(JComponent pComp) {
        if (pComp instanceof JTable) {
            ElementsTable table = (ElementsTable) pComp;
            Object element = table.getSelectedElement();
            if (element instanceof MasterDocument) {
                MasterDocument mdoc = (MasterDocument) element;
                return new MDocSelection(mdoc);
            }
        }
        return null;
    }
}
