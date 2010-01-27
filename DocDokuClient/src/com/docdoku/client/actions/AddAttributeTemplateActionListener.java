package com.docdoku.client.actions;


import com.docdoku.client.ui.common.CreateAttributeTemplateDialog;
import com.docdoku.client.ui.template.EditAttributeTemplatesPanel;
import com.docdoku.core.entities.InstanceAttributeTemplate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class AddAttributeTemplateActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditAttributeTemplatesPanel sourcePanel = (EditAttributeTemplatesPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                CreateAttributeTemplateDialog source = (CreateAttributeTemplateDialog) pAE.getSource();
                InstanceAttributeTemplate attr = new InstanceAttributeTemplate();
                attr.setName(source.getAttributeName());
                attr.setAttributeType(source.getAttributeType());
                sourcePanel.getAttributesListModel().addElement(attr);
            }
        };
        new CreateAttributeTemplateDialog(owner, action);
    }
}