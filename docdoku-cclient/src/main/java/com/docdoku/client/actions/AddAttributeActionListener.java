/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

package com.docdoku.client.actions;


import com.docdoku.client.ui.common.CreateAttributeTemplateDialog;
import com.docdoku.client.ui.doc.EditAttributesPanel;
import com.docdoku.core.document.InstanceAttributeTemplate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

public class AddAttributeActionListener implements ActionListener {


    @Override
    public void actionPerformed(ActionEvent pAE) {
        final EditAttributesPanel sourcePanel = (EditAttributesPanel) pAE.getSource();
        Dialog owner = (Dialog) SwingUtilities.getAncestorOfClass(Dialog.class, sourcePanel);
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent pAE) {
                CreateAttributeTemplateDialog source = (CreateAttributeTemplateDialog) pAE.getSource();
                InstanceAttributeTemplate attr = new InstanceAttributeTemplate();
                attr.setName(source.getAttributeName());
                attr.setAttributeType(source.getAttributeType());
                sourcePanel.addAttributePanel(attr.createInstanceAttribute());
            }
        };
        new CreateAttributeTemplateDialog(owner, action);
    }
}