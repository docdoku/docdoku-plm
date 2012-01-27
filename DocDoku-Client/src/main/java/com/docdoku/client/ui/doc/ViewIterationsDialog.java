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

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.CloseButton;
import com.docdoku.core.document.Document;
import com.docdoku.core.document.DocumentMaster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.client.localization.I18N;

public class ViewIterationsDialog extends JDialog {
    private ViewIterationsPanel mIterationsPanel;
    private CloseButton mCloseButton;
    private JButton mViewDocButton;
    private DocumentMaster mWatchedDocM;

    public ViewIterationsDialog(
            Frame pOwner,
            DocumentMaster pWatchedDocM, final ActionListener pDownloadAction, final ActionListener pOpenAction) {
        super(pOwner, I18N.BUNDLE.getString("ViewIterationsDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mWatchedDocM = pWatchedDocM;
        mCloseButton = new CloseButton(this, I18N.BUNDLE.getString("Close_button"));

        Image img =
                Toolkit.getDefaultToolkit().getImage(ViewIterationsDialog.class.getResource(
                        "/com/docdoku/client/resources/icons/view_large.png"));
        ImageIcon viewIcon = new ImageIcon(img);

        mViewDocButton = new JButton(I18N.BUNDLE.getString("View_button"), viewIcon);
        mViewDocButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                Document doc = mIterationsPanel.getSelectedDoc();
                new ViewDocDetailsDialog(ViewIterationsDialog.this, doc, pDownloadAction, pOpenAction);
            }
        });
        mIterationsPanel = new ViewIterationsPanel(mWatchedDocM);
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mViewDocButton);
        mViewDocButton.setHorizontalAlignment(SwingConstants.LEFT);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mIterationsPanel, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mViewDocButton);
        southPanel.add(mCloseButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }
}
