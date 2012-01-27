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

import com.docdoku.client.data.MainModel;
import com.docdoku.core.document.Document;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.util.FileIO;
import com.docdoku.core.common.BinaryResource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InterruptedIOException;

import javax.swing.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ViewFilesPanel;


public class DownloadActionListener implements ActionListener {
    private JFileChooser mFileChooser = new JFileChooser();
    
    @Override
    public void actionPerformed(ActionEvent pAE) {
        final ViewFilesPanel source = (ViewFilesPanel) pAE.getSource();
        final BinaryResource remoteFile = source.getSelectedFile();
        mFileChooser.setSelectedFile(new File(source.getSelectedFile().getName()));
        int state = mFileChooser.showSaveDialog(source);
        if (state == JFileChooser.APPROVE_OPTION) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File localFile;
                        FileHolder holder = source.getFileHolder();
                        if(holder instanceof Document)
                            localFile = MainModel.getInstance().getFile(source,(Document)holder,remoteFile);
                        else
                            localFile = MainModel.getInstance().getFile(source,(DocumentMasterTemplate)holder,remoteFile);
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

