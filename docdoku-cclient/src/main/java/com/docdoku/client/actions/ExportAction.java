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
import com.docdoku.client.data.MainModel;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.document.DocumentMaster;
import java.awt.event.ActionEvent;
import java.io.InterruptedIOException;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.core.document.DocumentIteration;
import com.l2fprod.common.swing.JDirectoryChooser;
import java.io.File;
import javax.swing.JFileChooser;

public class ExportAction extends ClientAbstractAction {

    private JDirectoryChooser mDirectoryChooser = new JDirectoryChooser();

    public ExportAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Export_title"), "/com/docdoku/client/resources/icons/export1.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Export_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("Export_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Export_mnemonic_key")));
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        final FolderTreeNode folderToExport = mOwner.getSelectedFolder();
        int state = mDirectoryChooser.showSaveDialog(mOwner);
        if (state == JFileChooser.APPROVE_OPTION) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File directory=mDirectoryChooser.getSelectedFile();
                        downloadFolder(folderToExport,directory);


                    } catch (InterruptedIOException pIIOEx) {

                    } catch (Exception pEx) {
                        String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                        JOptionPane.showMessageDialog(null,
                                message, I18N.BUNDLE.getString("Error_title"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            worker.start();
        }
    }

    
    private void downloadFolder(FolderTreeNode folder, File directory) throws Exception {
        for (int i = 0; i < folder.elementSize(); i++) {
            Object element = folder.getElementChild(i);
            if (element instanceof DocumentMaster) {
                DocumentMaster docM = (DocumentMaster) element;
                DocumentIteration doc = null;
                if(docM.isCheckedOut() && docM.getCheckOutUser().equals(MainModel.getInstance().getUser())){
                    int iteration = docM.getNumberOfIterations()-1;
                    if(iteration >0)
                        doc = docM.getIteration(iteration);
                }else
                    doc = docM.getLastIteration();
                if(doc!=null){
                    for (BinaryResource bin : doc.getAttachedFiles()) {
                        File destFolder=new File(directory, folder.getCompletePath().replace('/', File.separatorChar) + File.separator + docM.getId() + "-" + docM.getVersion() + File.separator + doc.getIteration());
                        File destFile = new File(destFolder, bin.getName());
                        if(!destFile.exists()){
                            File localFile = MainModel.getInstance().getFile(mOwner, doc, bin);
                            FileIO.copyFile(localFile, destFile);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < folder.folderSize(); i++) {
            FolderTreeNode subFolder = folder.getFolderChild(i);
            if(subFolder.getClass().equals(FolderTreeNode.class))
                downloadFolder(subFolder, directory);
        }
    }
}
