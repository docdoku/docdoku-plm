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

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.WebLink;
import com.docdoku.core.document.Document;
import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.meta.InstanceBooleanAttribute;
import com.docdoku.core.meta.InstanceDateAttribute;
import com.docdoku.core.meta.InstanceURLAttribute;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Florent GARIN
 */
public class ViewAttributesPanel extends JPanel{
    
    private JScrollPane mAttributesScrollPane;
    private Map<String, InstanceAttribute> mAttributes=new HashMap<String, InstanceAttribute>();
    
    public ViewAttributesPanel(Document pDoc) {
        mAttributes=pDoc.getInstanceAttributes();
        mAttributesScrollPane = new JScrollPane();
        createLayout();
    }
    
    private void createLayout() {
        JPanel attrsPanel=new JPanel();
        attrsPanel.setLayout(new GridBagLayout());
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
        for(InstanceAttribute attr:mAttributes.values()){
            JLabel label=new JLabel(attr.getName() + " :");
            attrsPanel.add(label, constraints);
        }
             
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        for(InstanceAttribute attr:mAttributes.values()){
            if(attr instanceof InstanceBooleanAttribute){
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(((InstanceBooleanAttribute)attr).isBooleanValue());
                checkBox.setEnabled(false);
                attrsPanel.add(checkBox, constraints);
            }
            else if(attr instanceof InstanceDateAttribute){
                DateFormat format=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.SHORT);
                JLabel label = new JLabel();
                Date date = ((InstanceDateAttribute)attr).getDateValue();
                if(date!=null)
                    label.setText(format.format(date));
                attrsPanel.add(label, constraints);
            }else if(attr instanceof InstanceURLAttribute){
                WebLink link = new WebLink();
                if(attr.getValue()!=null)
                    link.setLink(attr.getValue().toString(),attr.getValue().toString());
                attrsPanel.add(link, constraints);
            }
            else{
                JLabel label = new JLabel();
                if(attr.getValue()!=null)
                    label.setText(attr.getValue().toString());
                attrsPanel.add(label, constraints);
            }
            constraints.gridy = GridBagConstraints.RELATIVE;
        }
        mAttributesScrollPane.getViewport().add(attrsPanel);
        setLayout(new BorderLayout());
        add(mAttributesScrollPane, BorderLayout.NORTH);
    }
}
