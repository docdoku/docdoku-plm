package com.docdoku.client.actions;

import com.docdoku.client.data.MainModel;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.FileHolder;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.entities.BinaryResource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InterruptedIOException;
import java.nio.channels.FileChannel;

import javax.swing.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ViewFilesPanel;


public class DownloadActionListener implements ActionListener {
    private JFileChooser mFileChooser = new JFileChooser();
    
    public void actionPerformed(ActionEvent pAE) {
        final ViewFilesPanel source = (ViewFilesPanel) pAE.getSource();
        final BinaryResource remoteFile = source.getSelectedFile();
        mFileChooser.setSelectedFile(new File(source.getSelectedFile().getName()));
        int state = mFileChooser.showSaveDialog(source);
        if (state == JFileChooser.APPROVE_OPTION) {
            Thread worker = new Thread(new Runnable() {
                public void run() {
                    try {
                        File localFile;
                        FileHolder holder = source.getFileHolder();
                        if(holder instanceof Document)
                            localFile = MainModel.getInstance().getFile(source,(Document)holder,remoteFile);
                        else
                            localFile = MainModel.getInstance().getFile(source,(MasterDocumentTemplate)holder,remoteFile);
                        File destFile = mFileChooser.getSelectedFile();
                        FileIO.copyFile(localFile,destFile);
                    } catch(InterruptedIOException pIIOEx){
                        
                    }catch (Exception pEx) {
                        String message = pEx.getMessage()==null?I18N.BUNDLE
                                .getString("Error_unknown"):pEx.getMessage();
                        JOptionPane.showMessageDialog(null,
                                message, I18N.BUNDLE
                                .getString("Error_title"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            worker.start();
        }
    }
}

