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

package com.docdoku.client.ui.common;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.InstanceAttributeTemplate;

import javax.swing.*;

import java.awt.*;


public class EditAttributeTemplatePanel extends JPanel {

    private JLabel mNameLabel;
    private JLabel mTypeLabel;

    private JTextField mNameText;
    private JComboBox mTypeList;

    public EditAttributeTemplatePanel() {
        this("");
    }


    public EditAttributeTemplatePanel(InstanceAttributeTemplate pAttribute){
        this(pAttribute.getName());
        mTypeList.setSelectedItem(pAttribute.getAttributeType());
    }

    private EditAttributeTemplatePanel(String pName) {
        mNameLabel = new JLabel(I18N.BUNDLE.getString("NameMandatory_label"));
        mTypeLabel = new JLabel(I18N.BUNDLE.getString("TypeMandatory_label"));
        mNameText = new JTextField(new MaxLengthDocument(50), pName, 10);
        mTypeList =
                new JComboBox(InstanceAttributeTemplate.AttributeType.values());
        createLayout();
    }

    public String getNameAttribute() {
        return mNameText.getText();
    }


    public InstanceAttributeTemplate.AttributeType geType() {
        return (InstanceAttributeTemplate.AttributeType) mTypeList.getSelectedItem();
    }

    public JTextField getNameText() {
        return mNameText;
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Attribute_border")));
        mNameLabel.setLabelFor(mNameText);
        mTypeLabel.setLabelFor(mTypeList);

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
        add(mNameLabel, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mTypeLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mNameText, constraints);

        constraints.gridy = GridBagConstraints.RELATIVE;
        add(mTypeList, constraints);       
    }
}