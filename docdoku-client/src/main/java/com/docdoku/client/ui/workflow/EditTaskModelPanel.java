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

package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.workflow.TaskModel;
import com.docdoku.core.common.User;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.data.MainModel;

import javax.swing.*;

import java.awt.*;


public class EditTaskModelPanel extends JPanel {

    private JLabel mTitleLabel;
    private JLabel mInstructionsLabel;
    private JLabel mWorkerLabel;

    private JTextField mTitleText;
    private JTextArea mInstructionsTextArea;
    private JComboBox mWorkerList;

    public EditTaskModelPanel() {
        this("","");
    }


    public EditTaskModelPanel(TaskModel pTask){
        this(pTask.getTitle(),pTask.getInstructions());
        mWorkerList.setSelectedItem(pTask.getWorker());
    }

    private EditTaskModelPanel(String pTitle, String pInstructions) {
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("TitleMandatory_label"));
        mInstructionsLabel = new JLabel(I18N.BUNDLE.getString("Instructions_label"));
        mWorkerLabel = new JLabel(I18N.BUNDLE.getString("WorkerMandatory_label"));
        mTitleText = new JTextField(new MaxLengthDocument(50), pTitle, 10);
        mInstructionsTextArea=new JTextArea(new MaxLengthDocument(4096), pInstructions,10,35);
        mInstructionsTextArea.setLineWrap(true);
        mInstructionsTextArea.setWrapStyleWord(true);
        mWorkerList =
                new JComboBox(MainModel.getInstance().getUsers());
        mWorkerList.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
                String label;
                if(value instanceof User){
                    User worker = (User)value;
                    label = worker.getName();
                }else
                    label = value + "";
                setText(label);
                return this;
            }
        });
        createLayout();
    }

    public String getTitle() {
        return mTitleText.getText();
    }

    public String getInstructions() {
        return mInstructionsTextArea.getText();
    }

    public User getUser() {
        return (User) mWorkerList.getSelectedItem();
    }

    public JTextField getTitleText() {
        return mTitleText;
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Task_border")));
        mTitleLabel.setLabelFor(mTitleText);
        mInstructionsLabel.setLabelFor(mInstructionsTextArea);
        mWorkerLabel.setLabelFor(mWorkerList);

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

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mWorkerLabel, constraints);
        add(mInstructionsLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mTitleText, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mWorkerList, constraints);
        
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(mInstructionsTextArea), constraints);
        
    }


}
