package com.docdoku.client.ui.template;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.entities.MasterDocumentTemplate;

import javax.swing.*;
import java.awt.*;

public class ViewMDocTemplatePanel extends MDocTemplatePanel{
    
    private JLabel mIDValueLabel;
    private JLabel mDocumentTypeValueLabel;
    private JLabel mMaskValueLabel;
    
    public ViewMDocTemplatePanel(MasterDocumentTemplate pTemplate) {
        super();
        mIDValueLabel = new JLabel(pTemplate.getId());
        mDocumentTypeValueLabel = new JLabel(pTemplate.getDocumentType());
        mMaskValueLabel = new JLabel(pTemplate.getMask());
        mIdGenerated.setSelected(pTemplate.isIdGenerated());
        mIdGenerated.setEnabled(false);
        createLayout();
    }
    
    
    private void createLayout() {   
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
        add(mIDValueLabel, constraints);
        
        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mDocumentTypeValueLabel, constraints); 
        add(mMaskValueLabel, constraints);       
    }
}