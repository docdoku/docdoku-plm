/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.workflow.SerialActivityModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditSerialActivityModelPanel extends EditActivityModelPanel {

    private SerialActivityModel mActivityModel;
    private JScrollPane mTasksScrollPane;
    
    private JButton mUpButton;
    private JButton mDownButton;


    public EditSerialActivityModelPanel(SerialActivityModel pActivityModel, ActionListener pAddTaskAction, ActionListener pEditTaskAction) {
        super(pActivityModel, pAddTaskAction, pEditTaskAction);
        mActivityModel=pActivityModel;
        Image img = Toolkit.getDefaultToolkit().getImage(EditSerialActivityModelPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_up.png"));
        ImageIcon upIcon = new ImageIcon(img);

        img = Toolkit.getDefaultToolkit().getImage(EditSerialActivityModelPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_down.png"));
        ImageIcon downIcon = new ImageIcon(img);

        mUpButton = new JButton(I18N.BUNDLE.getString("MoveUp_button"), upIcon);
        mDownButton = new JButton(I18N.BUNDLE.getString("MoveDown_button"), downIcon);

        mTasksScrollPane = new JScrollPane();
        createLayout();
        createListener();
    }

    public SerialActivityModel getActivityModel(){
        return mActivityModel;
    }
    
    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Tasks_border")));
        
        mUpButton.setHorizontalAlignment(SwingConstants.LEFT);
        mDownButton.setHorizontalAlignment(SwingConstants.LEFT);
        
        mUpButton.setEnabled(false);
        mDownButton.setEnabled(false);

        mTasksScrollPane.getViewport().add(mTaskModelsList);
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = GUIConstants.INSETS;
        constraints.gridwidth = 1;

        constraints.gridheight = 5;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        add(mTasksScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridx = 1;
        add(mEditButton, constraints);

        constraints.gridy = 1;
        add(mAddButton, constraints);

        constraints.gridy = 2;
        add(mRemoveButton, constraints);

        constraints.gridy = 3;
        add(mUpButton, constraints);

        constraints.gridy = 4;
        add(mDownButton, constraints);
    }


    private void createListener() {
        mTaskModelsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                int numberOfSelection = mTaskModelsList.getSelectedIndices().length;
                switch (numberOfSelection) {
                    case 0:
                        mUpButton.setEnabled(false);
                        mDownButton.setEnabled(false);
                        break;
                    case 1:
                        boolean up = false;
                        boolean down = false;
                        if (mTaskModelsList.getSelectedIndex() > 0)
                            up = true;
                        if (mTaskModelsList.getSelectedIndex() < (mTaskModelsList.getModel().getSize() - 1))
                            down = true;

                        mUpButton.setEnabled(up);
                        mDownButton.setEnabled(down);
                        break;
                    default:
                        mUpButton.setEnabled(false);
                        mDownButton.setEnabled(false);
                }
            }
        });
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                mUpButton.setEnabled(false);
                mDownButton.setEnabled(false);
            }
        });
        mUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                int selectedIndex = mTaskModelsList.getSelectedIndex();
                mActivityModel.moveUpTaskModel(selectedIndex);
                mTaskModelsList.moveUpTaskModel(selectedIndex);
            }
        });
        mDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                int selectedIndex = mTaskModelsList.getSelectedIndex();
                mActivityModel.moveDownTaskModel(selectedIndex);
                mTaskModelsList.moveDownTaskModel(selectedIndex);
            }
        });

    }
}
