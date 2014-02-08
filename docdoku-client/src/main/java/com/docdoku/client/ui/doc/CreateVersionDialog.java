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
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.WorkflowModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateVersionDialog extends JDialog implements ActionListener {
    private CreateVersionPanel mCreateVersionPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private JTextArea mDescriptionTextArea;
    private JTabbedPane mTabbedPane;
    private DocumentMaster mOriginDocM;

    public CreateVersionDialog(Frame pOwner, DocumentMaster pOriginDocM, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("CreateVersionDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mOriginDocM=pOriginDocM;
        mCreateVersionPanel = new CreateVersionPanel(pOriginDocM);
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
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("CreateDocMPanel_border")));
        mDescriptionTextArea.setLineWrap(true);
        mDescriptionTextArea.setWrapStyleWord(true);
        mTabbedPane.add(I18N.BUNDLE.getString("Main_border"), mCreateVersionPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Description_border"), new JScrollPane(mDescriptionTextArea));
        
        mainPanel.add(mTabbedPane, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public String getDocMTitle() {
        return mCreateVersionPanel.getTitle();
    }
    
    public String getDescription() {
        return mDescriptionTextArea.getText();
    }
    
    public WorkflowModel getWorkflowModel() {
        return mCreateVersionPanel.getWorkflowModel();
    }

    public DocumentMaster getOriginDocM() {
        return mOriginDocM;
    }


    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
