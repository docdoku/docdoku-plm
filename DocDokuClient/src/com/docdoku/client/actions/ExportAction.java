package com.docdoku.client.actions;

import com.docdoku.client.data.Config;
import com.docdoku.client.data.FolderTreeNode;
import com.docdoku.client.data.HomeTreeNode;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.MasterDocument;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InterruptedIOException;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.core.entities.Document;
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

    public void actionPerformed(ActionEvent pAE) {
        final FolderTreeNode folderToExport = mOwner.getSelectedFolder();
        int state = mDirectoryChooser.showSaveDialog(mOwner);
        if (state == JFileChooser.APPROVE_OPTION) {
            Thread worker = new Thread(new Runnable() {
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
            if (element instanceof MasterDocument) {
                MasterDocument mdoc = (MasterDocument) element;
                Document doc = null;
                if(mdoc.isCheckedOut() && mdoc.getCheckOutUser().equals(MainModel.getInstance().getUser())){
                    int iteration = mdoc.getNumberOfIterations()-1;
                    if(iteration >0)
                        doc = mdoc.getIteration(iteration);
                }else
                    doc = mdoc.getLastIteration();
                if(doc!=null){
                    for (BinaryResource bin : doc.getAttachedFiles()) {
                        File destFolder=new File(directory, folder.getCompletePath().replace('/', File.separatorChar) + File.separator + mdoc.getId() + "-" + mdoc.getVersion() + File.separator + doc.getIteration());
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
