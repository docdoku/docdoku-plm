/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.EditFilesPanel;
import com.docdoku.client.ui.doc.ScanDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class ScanActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent pAE) {
        final EditFilesPanel sourcePanel = (EditFilesPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent pAE) {
                ScanDialog source = (ScanDialog) pAE.getSource();
                try {
                    source.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    //String id = source.getId();
                    //String title = source.getMDocTitle();
                 
                } catch (Exception pEx) {
                    String message = pEx.getMessage() == null ? I18N.BUNDLE.getString("Error_unknown") : pEx.getMessage();
                    JOptionPane.showMessageDialog(null,
                            message, I18N.BUNDLE.getString("Error_title"),
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    source.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        new ScanDialog(owner, action);
    }
}
