/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.client.ui.template;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.MaxLengthDocument;
import com.docdoku.core.document.MasterDocumentTemplate;

import javax.swing.*;
import java.awt.*;

public class EditMDocTemplatePanel extends MDocTemplatePanel{
    
    private JLabel mIDValueLabel;
    private JTextField mDocumentTypeText;
    private JTextField mMaskText;
    
    public EditMDocTemplatePanel(MasterDocumentTemplate pTemplate) {
        super();
        mIDValueLabel = new JLabel(pTemplate.getId());
        mDocumentTypeText = new JTextField(new MaxLengthDocument(50), pTemplate.getDocumentType(), 10);
        mMaskText = new JTextField(new MaxLengthDocument(50), pTemplate.getMask(), 10);
        mIdGenerated.setSelected(pTemplate.isIdGenerated());
        
        createLayout();
    }
    
    public String getMask() {
        return mMaskText.getText();
    }
    
    public String getDocumentType() {
        return mDocumentTypeText.getText();
    }
    
    private void createLayout() {   
        GridBagConstraints constraints = new GridBagConstraints();
        mDocumentTypeLabel.setLabelFor(mDocumentTypeText);        
        mMaskLabel.setLabelFor(mMaskText);
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
        add(mDocumentTypeText, constraints);        
        add(mMaskText, constraints);
    }
}