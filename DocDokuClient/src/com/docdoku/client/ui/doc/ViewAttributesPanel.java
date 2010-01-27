/*
 * ViewAttributesPanel.java
 *
 * Created on 27 septembre 2007, 22:18
 *
 */

package com.docdoku.client.ui.doc;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.common.WebLink;
import com.docdoku.core.entities.Document;
import com.docdoku.core.entities.InstanceAttribute;
import com.docdoku.core.entities.InstanceBooleanAttribute;
import com.docdoku.core.entities.InstanceDateAttribute;
import com.docdoku.core.entities.InstanceURLAttribute;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Florent.Garin
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
                DateFormat format=DateFormat.getInstance();
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
