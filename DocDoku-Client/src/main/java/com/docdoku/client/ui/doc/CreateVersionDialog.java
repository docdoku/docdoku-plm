/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.document.MasterDocument;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.docdoku.core.workflow.WorkflowModel;

public class CreateVersionDialog extends JDialog implements ActionListener {
    private CreateVersionPanel mCreateVersionPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private JTextArea mDescriptionTextArea;
    private JTabbedPane mTabbedPane;
    private MasterDocument mOriginMDoc;

    public CreateVersionDialog(Frame pOwner, MasterDocument pOriginMDoc, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("CreateVersionDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mOriginMDoc=pOriginMDoc;
        mCreateVersionPanel = new CreateVersionPanel(pOriginMDoc);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mTabbedPane = new JTabbedPane();
        mDescriptionTextArea=new JTextArea(new MaxLengthDocument(4096), "",10,35);
        mAction = pAction;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("CreateMDocPanel_border")));
        mDescriptionTextArea.setLineWrap(true);
        mDescriptionTextArea.setWrapStyleWord(true);
        mTabbedPane.add(I18N.BUNDLE.getString("Main_border"), mCreateVersionPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Description_border"), new JScrollPane(mDescriptionTextArea));
        
        mainPanel.add(mTabbedPane, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public String getMDocTitle() {
        return mCreateVersionPanel.getTitle();
    }
    
    public String getDescription() {
        return mDescriptionTextArea.getText();
    }
    
    public WorkflowModel getWorkflowModel() {
        return mCreateVersionPanel.getWorkflowModel();
    }

    public MasterDocument getOriginMDoc() {
        return mOriginMDoc;
    }


    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
