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

import com.docdoku.client.data.Config;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.doc.ScanDialog;
import com.docdoku.core.util.FileIO;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.File;

public class ScanActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent pAE) {
        final EditFilesPanel sourcePanel = (EditFilesPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        final ScannerDevice device = ScannerDevice.getInstance();
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent pAE) {
                final ScanDialog source = (ScanDialog) pAE.getSource();
                try {
                    String deviceName = source.getSelectedDevice();
                    String fileName = source.getFileName();
                    String format = source.getFileFormat();
                    String extension = FileIO.getExtension(fileName);
                    if(!format.equalsIgnoreCase(extension)){
                        fileName=fileName+"."+format;
                    }

                    device.select(deviceName);
                    final File scanFile = Config.getTempScanFile(fileName);
                    File folder = scanFile.getParentFile();
                    folder.mkdirs();
                    scanFile.deleteOnExit();
                    ActionListener callbackAction = new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent pAE) {
                            if(Boolean.parseBoolean(pAE.getActionCommand()))
                                sourcePanel.addFile(scanFile);
                        }
                    };
                    device.scan(scanFile, callbackAction);
                } catch (Exception pEx) {
                    String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE.getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        new ScanDialog(owner, action, device.getDeviceNames(), device.getCurrentDeviceName(), device.getCurrenFormat());
    }
}
