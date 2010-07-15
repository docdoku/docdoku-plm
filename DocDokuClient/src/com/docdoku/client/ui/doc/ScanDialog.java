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
package com.docdoku.client.ui.doc;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.ImageIcon.*;

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
            ActionListener pOKAction) {
        super(pOwner, I18N.BUNDLE.getString("ScanDialog_title"), true);
        setLocationRelativeTo(pOwner);

        mScanPanel=new ScanPanel();
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

    @Override
    public void actionPerformed(ActionEvent pAE) {
        mOKAction.actionPerformed(new ActionEvent(this, 0, null));
    }

    private void createListener() {
        
    }
}











    

