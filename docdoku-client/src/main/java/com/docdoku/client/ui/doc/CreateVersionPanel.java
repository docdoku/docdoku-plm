/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.common.Version;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;

public class CreateVersionPanel extends JPanel {

    private JLabel mAuthorLabel;
    private JLabel mVersionLabel;
    private JLabel mIDLabel;
    private JLabel mTitleLabel;
    private JLabel mWorkflowModelLabel;
    private JLabel mAuthorValueLabel;
    private JLabel mVersionValueLabel;
    private JLabel mIDValueLabel;
    
    private JTextField mTitleText;
    private JComboBox mWorkflowModelList;

    public CreateVersionPanel(DocumentMaster pOriginDocM) {
        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        mVersionLabel = new JLabel(I18N.BUNDLE.getString("Version_label"));
        mIDLabel = new JLabel(I18N.BUNDLE.getString("ID_label"));
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("Title_label"));
        mWorkflowModelLabel = new JLabel(I18N.BUNDLE.getString("Workflow_label"));
        mAuthorValueLabel = new JLabel(MainModel.getInstance().getUser().getName());
        Version nextVersion=new Version(pOriginDocM.getVersion());
        nextVersion.increase();
        mVersionValueLabel = new JLabel(nextVersion.toString());
        mIDValueLabel = new JLabel(pOriginDocM.getId());
        WorkflowModel[] models=MainModel.getInstance().getWorkflowModels();      
        Object[] comboBoxValues = new Object[models.length + 1];
        comboBoxValues[0] = I18N.BUNDLE.getString("None_label");
        int i = 1;
        for (WorkflowModel model : models)
            comboBoxValues[i++] = model;
        
        mWorkflowModelList =
                new JComboBox(comboBoxValues);
        
        mTitleText = new JTextField(new MaxLengthDocument(50), "", 10);
        createLayout();
    }

    public String getTitle() {
        return mTitleText.getText();
    }


    public WorkflowModel getWorkflowModel() {     
        Object selectedItem = mWorkflowModelList.getSelectedItem();
        if (selectedItem instanceof WorkflowModel)
            return (WorkflowModel) selectedItem;
        else
            return null;
    }

    private void createLayout() {
        mTitleLabel.setLabelFor(mTitleText);
        mWorkflowModelLabel.setLabelFor(mWorkflowModelList);

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;

        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;

        add(mAuthorLabel, constraints);
        add(mVersionLabel, constraints);
        add(mIDLabel, constraints);
        add(mTitleLabel, constraints);
        add(mWorkflowModelLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mAuthorValueLabel, constraints);
        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mVersionValueLabel, constraints);
        add(mIDValueLabel, constraints);
        add(mTitleText, constraints);
        add(mWorkflowModelList, constraints);
    }
}
