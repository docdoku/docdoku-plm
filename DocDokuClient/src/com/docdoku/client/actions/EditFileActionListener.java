package com.docdoku.client.actions;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.core.entities.BinaryResource;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.FileHolder;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.MasterDocumentTemplate;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;

import java.awt.Desktop;

import javax.swing.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.data.Config;


public class EditFileActionListener implements ActionListener {
    public void actionPerformed(ActionEvent pAE) {
        final EditFilesPanel source = (EditFilesPanel) pAE.getSource();
        final Object selectedFile = source.getSelectedFile();
        
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    File localFile;
                    if(selectedFile instanceof BinaryResource){
                        BinaryResource remoteFile = (BinaryResource)selectedFile;
                        FileHolder holder = source.getFileHolder();
                        if(holder instanceof Document)
                            localFile = MainModel.getInstance().getFile(source,(Document)holder,remoteFile);
                        else
                            localFile = MainModel.getInstance().getFile(source,(MasterDocumentTemplate)holder,remoteFile);

                        if(source.getFilesToUpdate().get(remoteFile)==null)
                            source.getFilesToUpdate().put(remoteFile,localFile.lastModified());
                        
                    }else{
                        localFile=(File)selectedFile;
                    }
                    Desktop desktop=Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.EDIT)){
                        try{
                            desktop.edit(localFile);
                        }catch(IOException pEx){
                            //Some files fail to be edited, so try to simply open them.
                            desktop.open(localFile);
                        }
                    }
                    else
                        desktop.open(localFile);
                    
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