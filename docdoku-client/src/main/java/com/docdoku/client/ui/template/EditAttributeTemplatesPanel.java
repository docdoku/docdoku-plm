/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.ui.template;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.document.DocumentMasterTemplate;
import com.docdoku.core.meta.InstanceAttributeTemplate;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditAttributeTemplatesPanel extends JPanel implements ActionListener {

    private JScrollPane mAttributesScrollPane;
    private JList mAttributesList;
    private JButton mAddButton;
    private JButton mRemoveButton;
    private DefaultListModel mAttributesListModel;
    private DocumentMasterTemplate mEditedDocMTemplate;
    private ActionListener mAddAttributeAction;

    public EditAttributeTemplatesPanel(DocumentMasterTemplate pEditedDocMTemplate,ActionListener pAddAttributeAction) {
        this(pAddAttributeAction);
        mEditedDocMTemplate = pEditedDocMTemplate;
        for(InstanceAttributeTemplate attr:mEditedDocMTemplate.getAttributeTemplates()) {
            mAttributesListModel.addElement(attr);
        }
    }
    
    public EditAttributeTemplatesPanel(ActionListener pAddAttributeAction) {
        mAttributesListModel = new DefaultListModel();
        mAddAttributeAction =pAddAttributeAction;
        Image img =
                Toolkit.getDefaultToolkit().getImage(EditAttributeTemplatesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditAttributeTemplatesPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);

        mAddButton = new JButton(I18N.BUNDLE.getString("AddAttribute_button"), addIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveAttribute_button"), removeIcon);
        
        mAttributesScrollPane = new JScrollPane();
        mAttributesList = new JList(mAttributesListModel);
        createLayout();
        createListener();
    }



    public DefaultListModel getAttributesListModel() {
        return mAttributesListModel;
    }

    private void createLayout() {
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setEnabled(false);
        mAttributesScrollPane.getViewport().add(mAttributesList);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridwidth = 1;

        constraints.gridheight = 4;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(mAttributesScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridx = 1;
        add(mAddButton, constraints);

        constraints.gridy = 1;
        add(mRemoveButton, constraints);
    }

    public DocumentMasterTemplate getEditedDocMTemplate() {
        return mEditedDocMTemplate;
    }



    private void createListener() {
        mAttributesList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                mRemoveButton.setEnabled(!mAttributesList.isSelectionEmpty());
            }
        });
        mAddButton.addActionListener(this);
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                Object[] selectedObjects = mAttributesList.getSelectedValues();
                for (int i = 0; i < selectedObjects.length; i++) {
                    mAttributesListModel.removeElement(selectedObjects[i]);
                }
                mRemoveButton.setEnabled(false);
            }
        });
    }

    public void actionPerformed(ActionEvent pAE) {
        mAddAttributeAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
