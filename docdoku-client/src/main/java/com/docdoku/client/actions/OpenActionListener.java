/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.actions;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.ViewFilesPanel;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.FileHolder;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentMasterTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InterruptedIOException;

public class OpenActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent pAE) {
        final ViewFilesPanel source = (ViewFilesPanel) pAE.getSource();
        final BinaryResource remoteFile = source
                .getSelectedFile();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File localFile;
                    FileHolder holder = source.getFileHolder();
                    if(holder instanceof DocumentIteration)
                        localFile = MainModel.getInstance().getFile(source,(DocumentIteration)holder,remoteFile);
                    else
                        localFile = MainModel.getInstance().getFile(source,(DocumentMasterTemplate)holder,remoteFile);
                    
                    Desktop.getDesktop().open(localFile);
                } catch(InterruptedIOException pIIOEx){
                    
                } catch (Exception pEx) {
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
