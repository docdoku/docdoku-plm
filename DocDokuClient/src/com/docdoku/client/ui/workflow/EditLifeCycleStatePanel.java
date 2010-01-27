package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;

import javax.swing.*;
import java.awt.*;



public class EditLifeCycleStatePanel extends JPanel{

    private JLabel mStateLabel;
    private JTextField mStateText;


    public EditLifeCycleStatePanel(String pState) {
        mStateLabel=new JLabel(I18N.BUNDLE.getString("State_label"));
        mStateText=new JTextField(new MaxLengthDocument(50), pState, 10);
        createLayout();
    }

    public String getState(){
        return mStateText.getText();
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("State_border")));
        mStateLabel.setLabelFor(mStateText);
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
        add(mStateLabel, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mStateText, constraints);
    }


}
