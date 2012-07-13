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

package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.client.ui.workflow.EditActivityModelPanel;
import com.docdoku.core.workflow.ParallelActivityModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;


public class EditParallelActivityModelPanel extends EditActivityModelPanel {
    
    private ParallelActivityModel mActivityModel;
    private JScrollPane mTasksScrollPane;
    
    private JSpinner mNumberOfNeededCompletedTasksSpinner;
    private JLabel mRequiredTasksLabel;
    private SpinnerNumberModel mNumberOfNeededCompletedTasksModel;


    public EditParallelActivityModelPanel(ParallelActivityModel pActivityModel,ActionListener pAddTaskAction, ActionListener pEditTaskAction) {
        super(pActivityModel, pAddTaskAction, pEditTaskAction);
        mActivityModel=pActivityModel;
        mNumberOfNeededCompletedTasksModel = new SpinnerRangeListModel(mTaskModelsList.getModel());

        mNumberOfNeededCompletedTasksSpinner = new JSpinner(mNumberOfNeededCompletedTasksModel);
        mRequiredTasksLabel = new JLabel(I18N.BUNDLE.getString("TasksToComplete_label"));
        mRequiredTasksLabel.setLabelFor(mNumberOfNeededCompletedTasksSpinner);

        mTasksScrollPane = new JScrollPane();
        mNumberOfNeededCompletedTasksModel.setValue(new Integer(pActivityModel.getTasksToComplete()));
        createLayout();
    }

    public ParallelActivityModel getActivityModel(){
        return mActivityModel;
    }
    
    private void createLayout() {
        setBorder(BorderFactory.createTitledBorder(I18N.BUNDLE.getString("Tasks_border")));
        mTasksScrollPane.getViewport().add(mTaskModelsList);
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
        add(mTasksScrollPane, constraints);

        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 2;
        constraints.gridx = 1;
        add(mEditButton, constraints);

        constraints.gridy = 1;
        add(mAddButton, constraints);

        constraints.gridy = 2;
        add(mRemoveButton, constraints);

        constraints.gridy = 3;

        constraints.gridwidth = 1;
        add(mRequiredTasksLabel, constraints);

        constraints.gridx = 2;
        add(mNumberOfNeededCompletedTasksSpinner, constraints);
    }

    public int getNumberOfNeededCompletedTasks() {
        return mNumberOfNeededCompletedTasksModel.getNumber().intValue();
    }
}
