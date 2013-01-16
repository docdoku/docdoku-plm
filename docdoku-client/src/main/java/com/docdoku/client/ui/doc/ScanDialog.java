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
package com.docdoku.client.ui.doc;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.ImageIcon.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Gary Gautruche
 * @version 1.4, 15/07/10
 * @since   V1.4
 */
public class ScanDialog extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private ScanPanel mScanPanel;
    private ActionListener mOKAction;

    public ScanDialog(Dialog pOwner,
            ActionListener pOKAction, String[] pDeviceNames, String pDefaultDevice, String pDefaultFormat) {
        super(pOwner, I18N.BUNDLE.getString("ScanDialog_title"), true);
        setLocationRelativeTo(pOwner);

        mScanPanel = new ScanPanel(pDeviceNames, pDefaultDevice, pDefaultFormat);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mOKAction = pOKAction;
        createLayout();
        createListener();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("ScanPanel_border")));
        mainPanel.add(mScanPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        mOKCancelPanel.setEnabled(false);
        pack();

    }

    public String getFileName() {
        return mScanPanel.getFileName();
    }

    public String getFileFormat() {
        return mScanPanel.getFileFormat();
    }

    public String getSelectedDevice() {
        return mScanPanel.getSelectedDevice();
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        mOKAction.actionPerformed(new ActionEvent(this, 0, null));
    }

    private void createListener() {
        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent pDE) {
                mOKCancelPanel.setEnabled(true);
            }

            @Override
            public void removeUpdate(DocumentEvent pDE) {
                int length = pDE.getDocument().getLength();
                if (length == 0) {
                    mOKCancelPanel.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent pDE) {
            }
        };
        mScanPanel.getFileNameText().getDocument().addDocumentListener(docListener);
    }
}












