/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.client.actions;

import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ElementsTable;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.DocMSelection;
import com.docdoku.core.document.DocumentMaster;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;


public class DocMTransferHandler extends TransferHandler {
    
    private DataFlavor mDocMFlavor;
    
    public DocMTransferHandler() {
        try {
            mDocMFlavor = new DataFlavor(GUIConstants.DOCM_FLAVOR);
        } catch (ClassNotFoundException pCNFEx) {
            throw new RuntimeException("Unexpected error: unrecognized data flavor", pCNFEx);
        }
    }
    
    @Override
    public boolean canImport(JComponent pComp,
            DataFlavor[] pTransferFlavors) {
        
        if (pComp instanceof JTree) {
            for (DataFlavor flavor : pTransferFlavors) {
                if (flavor.equals(mDocMFlavor))
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
    
    @Override
    public int getSourceActions
            (JComponent
            pComp) {
        if (pComp instanceof JTable)
            return TransferHandler.MOVE;
        else
            return TransferHandler.NONE;
    }
    
    @Override
    public boolean importData
            (JComponent
            pComp, Transferable
            pTransferable) {
        try {
            if (pComp instanceof JTree) {
                DocumentMaster docM = (DocumentMaster) pTransferable.getTransferData(mDocMFlavor);
                JTree tree = (JTree) pComp;
                tree.getSelectionPath();
                String path = ((FolderTreeNode) tree.getLastSelectedPathComponent()).getCompletePath();
                MainController.getInstance().moveDocM(path, docM);
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

    @Override
    protected Transferable createTransferable(JComponent pComp) {
        if (pComp instanceof JTable) {
            ElementsTable table = (ElementsTable) pComp;
            Object element = table.getSelectedElement();
            if (element instanceof DocumentMaster) {
                DocumentMaster docM = (DocumentMaster) element;
                return new DocMSelection(docM);
            }
        }
        return null;
    }
}
