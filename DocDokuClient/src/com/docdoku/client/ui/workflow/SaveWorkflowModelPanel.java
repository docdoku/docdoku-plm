package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;
import java.awt.*;

public class SaveWorkflowModelPanel extends JPanel {

    private JLabel mWorkflowModelLabel;
    private JTextField mWorkflowModelText;

    public SaveWorkflowModelPanel(WorkflowModel pEditedWorkflowModel) {
        mWorkflowModelLabel = new JLabel(I18N.BUNDLE.getString("NameMandatory_label"));
        mWorkflowModelText = new JTextField(new MaxLengthDocument(50), pEditedWorkflowModel.getId(), 10);
        createLayout();
    }

    public JTextField getWorkflowModelText() {
        return mWorkflowModelText;
    }

    public String getWorkflowModelId() {
        return mWorkflowModelText.getText();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("WorkflowModel_border")));
        mWorkflowModelLabel.setLabelFor(mWorkflowModelText);

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
        add(mWorkflowModelLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mWorkflowModelText, constraints);
    }
}
