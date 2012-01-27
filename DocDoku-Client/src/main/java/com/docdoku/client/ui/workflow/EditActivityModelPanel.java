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
import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.TaskModel;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

public abstract class EditActivityModelPanel extends JPanel implements ActionListener {
    
    protected TaskModelsList mTaskModelsList;
    protected JButton mAddButton;
    protected JButton mEditButton;
    protected JButton mRemoveButton;
    protected ActionListener mAddTaskAction;
    protected ActionListener mEditTaskAction;
    
    public EditActivityModelPanel(ActivityModel pActivityModel, ActionListener pAddTaskAction, ActionListener pEditTaskAction) {
        mAddTaskAction=pAddTaskAction;
        mEditTaskAction = pEditTaskAction;
        mTaskModelsList = new TaskModelsList(pActivityModel.getTaskModels());
        
        Image img = Toolkit.getDefaultToolkit().getImage(EditActivityModelPanel.class.getResource("/com/docdoku/client/resources/icons/clipboard_large.png"));
        ImageIcon editIcon = new ImageIcon(img);
        
        img = Toolkit.getDefaultToolkit().getImage(EditActivityModelPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_plus.png"));
        ImageIcon addIcon = new ImageIcon(img);
        
        img = Toolkit.getDefaultToolkit().getImage(EditParallelActivityModelPanel.class.getResource("/com/docdoku/client/resources/icons/navigate_minus.png"));
        ImageIcon removeIcon = new ImageIcon(img);
        
        mAddButton = new JButton(I18N.BUNDLE.getString("AddTask_button"), addIcon);
        mEditButton = new JButton(I18N.BUNDLE.getString("EditTask_button"), editIcon);
        mRemoveButton = new JButton(I18N.BUNDLE.getString("RemoveTask_button"), removeIcon);
        createLayout();
        createListener();
    }
    
    private void createLayout() {
        mEditButton.setHorizontalAlignment(SwingConstants.LEFT);
        mAddButton.setHorizontalAlignment(SwingConstants.LEFT);
        mRemoveButton.setHorizontalAlignment(SwingConstants.LEFT);
        mEditButton.setEnabled(false);
        mRemoveButton.setEnabled(false);
    }
    
    private void createListener() {
        mTaskModelsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent pE) {
                int numberOfSelection = mTaskModelsList.getSelectedIndices().length;
                switch (numberOfSelection) {
                    case 0:
                        mRemoveButton.setEnabled(false);
                        mEditButton.setEnabled(false);
                        break;
                    case 1:
                        mEditButton.setEnabled(true);
                        mRemoveButton.setEnabled(true);
                        break;
                    default:
                        mEditButton.setEnabled(false);
                        mRemoveButton.setEnabled(true);
                        break;
                }
            }
        });
        mAddButton.addActionListener(this);
        mEditButton.addActionListener(this);
        mRemoveButton.addActionListener(this);
    }
    
    public abstract ActivityModel getActivityModel();
    
    public TaskModelsList getTasksList() {
        return mTaskModelsList;
    }
    
    public TaskModel[] getSelectedTaskModels() {
        return mTaskModelsList.getSelectedValues();
    }
    
    public void replaceSelectedTaskModel(TaskModel pNewTask){
        int index=mTaskModelsList.getSelectedIndex();
        mTaskModelsList.setTask(index,pNewTask);
    }
    
    public void actionPerformed(ActionEvent pAE) {
        String command = pAE.getActionCommand();
        if (command.equals(I18N.BUNDLE.getString("EditTask_button")))
            mEditTaskAction.actionPerformed(new ActionEvent(this, 0, null));
        else if(command.equals(I18N.BUNDLE.getString("AddTask_button")))
            mAddTaskAction.actionPerformed(new ActionEvent(this, 0, null));
        else if(command.equals(I18N.BUNDLE.getString("RemoveTask_button"))){
            TaskModel[] tasks=mTaskModelsList.getSelectedValues();
            for(TaskModel task:tasks){
                getActivityModel().removeTaskModel(task);
            }
            
            mTaskModelsList.removeSelectedValues();
            mRemoveButton.setEnabled(false);
        }
    }
}
