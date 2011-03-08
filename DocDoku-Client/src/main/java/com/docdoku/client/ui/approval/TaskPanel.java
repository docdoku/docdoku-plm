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

package com.docdoku.client.ui.approval;


import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.workflow.Task;
import java.util.Collection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class TaskPanel extends JPanel {

    private JLabel mTitleLabel;
    private JLabel mInstructionsLabel;
    private JComboBox mTitleComboBox;
    private JTextArea mInstructionsValueTextArea;
    private JLabel mCommentLabel;
    private JTextField mCommentText;

    public TaskPanel(Collection<Task> pTasks) {
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("Title_label"));
        mInstructionsLabel = new JLabel(I18N.BUNDLE.getString("Instructions_label"));
        mTitleComboBox = new JComboBox(pTasks.toArray(new Task[pTasks.size()]));
        mInstructionsValueTextArea=new JTextArea(pTasks.iterator().next().getInstructions(),10,20);
        mInstructionsValueTextArea.setLineWrap(true);
        mInstructionsValueTextArea.setWrapStyleWord(true);
        mInstructionsValueTextArea.setEditable(false);
        mCommentLabel = new JLabel(I18N.BUNDLE.getString("Comment_label"));
        mCommentText = new JTextField(new MaxLengthDocument(255), "", 10);
        createLayout();
        createListener();
    }

    public Task getTask() {
        return (Task) mTitleComboBox.getSelectedItem();
    }


    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Approval_border")));
        mTitleLabel.setLabelFor(mTitleComboBox);
        mInstructionsLabel.setLabelFor(mInstructionsValueTextArea);

        mCommentLabel.setLabelFor(mCommentText);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mTitleLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mTitleComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mInstructionsLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;   
        add(new JScrollPane(mInstructionsValueTextArea), constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mCommentLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL; 
        add(mCommentText, constraints);
    }

    public String getComment() {
        return mCommentText.getText();
    }
    
    private void createListener() {
        mTitleComboBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                mInstructionsValueTextArea.setText(getTask().getInstructions());
            }
        });
    }
}
