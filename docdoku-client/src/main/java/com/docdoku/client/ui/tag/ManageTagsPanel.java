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

package com.docdoku.client.ui.tag;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.document.Tag;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class ManageTagsPanel extends JPanel{

    private JScrollPane mTagsScrollPane;
    private JList mTagsList;
    private JButton mAddButton;
    private JButton mRemoveButton;
    private JLabel mTagLabel;
    private JComboBox mTagList;
    private DefaultListModel mTagsListModel;


    public ManageTagsPanel() {
        mTagsListModel = new DefaultListModel();
        Image img =
                Toolkit.getDefaultToolkit().getImage(ManageTagsPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(ManageTagsPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);

        mAddButton = new JButton(I18N.BUNDLE.getString("AddTag_button"), addIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveTag_button"), removeIcon);
        mTagLabel = new JLabel(I18N.BUNDLE.getString("Tags_label"));
        mTagList=new JComboBox(MainModel.getInstance().getTags());
        mTagsScrollPane = new JScrollPane();
        mTagsList = new JList(mTagsListModel);
        createLayout();
        createListener();
    }

    public ManageTagsPanel(Collection<Tag> pTags) {
        this();
        for(Tag tag:pTags) {
            mTagsListModel.addElement(tag.getLabel());
        }
    }


    public DefaultListModel getTagsListModel() {
        return mTagsListModel;
    }

    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Tags_border")));
        mTagLabel.setLabelFor(mTagList);
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setEnabled(false);
        mTagList.setEditable(true);
        mTagsScrollPane.getViewport().add(mTagsList);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = GUIConstants.INSETS;
        
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        add(mTagLabel, constraints);
        
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(mTagList, constraints);
        
        constraints.gridy = 1;
        constraints.gridx = 0;
        constraints.gridheight = 3;
        constraints.gridwidth = 2;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(mTagsScrollPane, constraints);
        
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 2;
        constraints.gridy = 0;
        add(mAddButton, constraints);

        constraints.gridy = 1;
        add(mRemoveButton, constraints);
    }


    private void createListener() {
        mTagsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                mRemoveButton.setEnabled(!mTagsList.isSelectionEmpty());
            }
        });
        mAddButton.addActionListener(new ActionListener() {         
            public void actionPerformed(ActionEvent pAE) {
                mTagsListModel.addElement(mTagList.getSelectedItem());
            }
        });
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                Object[] selectedObjects = mTagsList.getSelectedValues();
                for (int i = 0; i < selectedObjects.length; i++) {
                    mTagsListModel.removeElement(selectedObjects[i]);
                }
                mRemoveButton.setEnabled(false);
            }
        });
    }

}
