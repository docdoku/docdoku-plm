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
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.document.MasterDocumentTemplate;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.document.Folder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateMDocDialog extends JDialog implements ActionListener {
    private CreateMDocPanel mCreateMDocPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private JTextArea mDescriptionTextArea;
    private JTabbedPane mTabbedPane;
    private Folder mDestinationFolder;

    public CreateMDocDialog(Frame pOwner, Folder pDestinationFolder, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("CreateMDocDialog_title"), true);
        setLocationRelativeTo(pOwner);
        mDestinationFolder=pDestinationFolder;
        mCreateMDocPanel = new CreateMDocPanel();
        mOKCancelPanel = new OKCancelPanel(this, this);
        mTabbedPane = new JTabbedPane();
        mDescriptionTextArea=new JTextArea(new MaxLengthDocument(4096), "",10,35);
        mAction = pAction;
        createLayout();
        createListener();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout()); 
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("CreateMDocPanel_border")));
        mDescriptionTextArea.setLineWrap(true);
        mDescriptionTextArea.setWrapStyleWord(true);
        mTabbedPane.add(I18N.BUNDLE.getString("Main_border"), mCreateMDocPanel);
        mTabbedPane.add(I18N.BUNDLE.getString("Description_border"), new JScrollPane(mDescriptionTextArea));
        mainPanel.add(mTabbedPane, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        mOKCancelPanel.setEnabled(false);
        pack();
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
                if (length == 0)
                    mOKCancelPanel.setEnabled(false);
            }

            @Override
            public void changedUpdate(DocumentEvent pDE) {
            }
        };
        mCreateMDocPanel.getIDText().getDocument().addDocumentListener(docListener);
    }

    public Folder getDestinationFolder() {
        return mDestinationFolder;
    }

    public String getMDocID() {
        return mCreateMDocPanel.getID();
    }

    public String getMDocTitle() {
        return mCreateMDocPanel.getTitle();
    }

    public String getDescription() {
        return mDescriptionTextArea.getText();
    }
    
    public WorkflowModel getWorkflowModel() {
        return mCreateMDocPanel.getWorkflowModel();
    }
    
    public MasterDocumentTemplate getMDocTemplate() {
        return mCreateMDocPanel.getMDocTemplate();
    }

    @Override
    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
