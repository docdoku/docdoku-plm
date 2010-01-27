package com.docdoku.client.ui.template;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;

import javax.swing.*;
import java.awt.*;

public class CreateMDocTemplatePanel extends MDocTemplatePanel{
    
    private JTextField mIDText;
    private JTextField mDocumentTypeText;
    private JTextField mMaskText;
    
    public CreateMDocTemplatePanel() {
        super();
        mIDText = new JTextField(new MaxLengthDocument(50), "", 10);
        mDocumentTypeText = new JTextField(new MaxLengthDocument(50), "", 10);
        mMaskText = new JTextField(new MaxLengthDocument(50), "", 10);
        
        createLayout();
    }
    
    
    public String getID() {
        return mIDText.getText();
    }
    
    public JTextField getIDText() {
        return mIDText;
    }
    
    public String getMask() {
        return mMaskText.getText();
    }
    
    public String getDocumentType() {
        return mDocumentTypeText.getText();
    }
    
    private void createLayout() {
        mIDLabel.setLabelFor(mIDText);
        mDocumentTypeLabel.setLabelFor(mDocumentTypeText);        
        mMaskLabel.setLabelFor(mMaskText);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 0;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridy = 1;
        add(mIDText, constraints);
        
        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mDocumentTypeText, constraints);
        add(mMaskText, constraints);
    }
}