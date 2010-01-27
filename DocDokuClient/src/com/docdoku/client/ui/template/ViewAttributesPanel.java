package com.docdoku.client.ui.template;

import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.entities.InstanceAttributeTemplate;
import com.docdoku.core.entities.MasterDocumentTemplate;

import javax.swing.*;

import java.awt.*;

public class ViewAttributesPanel extends JPanel{
    
    private JScrollPane mAttributesScrollPane;
    private JList mAttributesList;
    private DefaultListModel mAttributesListModel;    
    
    public ViewAttributesPanel(MasterDocumentTemplate pEditedMDocTemplate) {
        mAttributesListModel = new DefaultListModel();
        mAttributesScrollPane = new JScrollPane();
        mAttributesList = new JList(mAttributesListModel);
        
        for(InstanceAttributeTemplate attr:pEditedMDocTemplate.getAttributeTemplates()) {
            mAttributesListModel.addElement(attr);
        }
        createLayout();
    }
    


    private void createLayout() {
        mAttributesScrollPane.getViewport().add(mAttributesList);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridwidth = 1;
        
        constraints.gridheight = 3;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(mAttributesScrollPane, constraints);

    }
    

}