package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.client.data.MainModel;
import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.core.entities.keys.Version;

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

    public CreateVersionPanel(MasterDocument pOriginMDoc) {
        mAuthorLabel = new JLabel(I18N.BUNDLE.getString("Author_label"));
        mVersionLabel = new JLabel(I18N.BUNDLE.getString("Version_label"));
        mIDLabel = new JLabel(I18N.BUNDLE.getString("ID_label"));
        mTitleLabel = new JLabel(I18N.BUNDLE.getString("Title_label"));
        mWorkflowModelLabel = new JLabel(I18N.BUNDLE.getString("Workflow_label"));
        mAuthorValueLabel = new JLabel(MainModel.getInstance().getUser().getName());
        Version nextVersion=new Version(pOriginMDoc.getVersion());
        nextVersion.increase();
        mVersionValueLabel = new JLabel(nextVersion.toString());
        mIDValueLabel = new JLabel(pOriginMDoc.getId());
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
