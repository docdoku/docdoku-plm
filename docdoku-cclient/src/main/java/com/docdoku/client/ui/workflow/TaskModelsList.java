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

import com.docdoku.core.workflow.TaskModel;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;


public class TaskModelsList extends JList {

    private DefaultListModel mTasksListModel = new DefaultListModel();
    private Map<String, TaskModel> mLabel2Task = new HashMap<String, TaskModel>();

    public TaskModelsList() {
        setModel(mTasksListModel);
        setCellRenderer(new TaskListCellRenderer());
    }

    public TaskModelsList(Collection<TaskModel> pTasks) {
        this();
        setTasks(pTasks);
    }

    public void setTask(int pIndex, TaskModel pTask){
        String label=pTask.getTitle() + "/" + pTask.getWorker();
        String oldLabel=(String)mTasksListModel.get(pIndex);
        mLabel2Task.remove(oldLabel);
        mLabel2Task.put(label,pTask);
        mTasksListModel.set(pIndex,label);
    }

    public void setTasks(Collection<TaskModel> pTasks) {
        mTasksListModel.clear();
        mLabel2Task.clear();
        Iterator<TaskModel> iti = pTasks.iterator();
        while (iti.hasNext()) {
            addTask(iti.next());
        }
    }

    public void addTask(TaskModel pTask) {
        String label=pTask.getTitle() + "/" + pTask.getWorker();
        mLabel2Task.put(label,pTask);
        mTasksListModel.addElement(label);
    }

    public void removeTask(TaskModel pTask) {
        String label=pTask.getTitle() + "/" + pTask.getWorker();
        mLabel2Task.remove(label);
        mTasksListModel.removeElement(label);
    }

    public void addTask(TaskModel[] pTasks) {
        for (TaskModel task : pTasks) {
            addTask(task);
        }
    }

    public void removeTask(TaskModel[] pTasks) {
        for (TaskModel task : pTasks) {
            removeTask(task);
        }
    }

    public void removeSelectedValues() {
        Object[] selectedValues = super.getSelectedValues();
        for (Object selectedValue : selectedValues) {
            mLabel2Task.remove(selectedValue);
            mTasksListModel.removeElement(selectedValue);
        }
    }

    public void moveUpTaskModel(int pSelectedIndex) {
        if (pSelectedIndex > 0) {
            Object selectedObject = mTasksListModel.remove(pSelectedIndex);
            mTasksListModel.add(--pSelectedIndex,selectedObject);
            setSelectedIndex(pSelectedIndex);
        }
    }

    public void moveDownTaskModel(int pSelectedIndex) {
        if (pSelectedIndex < mTasksListModel.size() - 1) {
            Object selectedObject = mTasksListModel.remove(pSelectedIndex);
            mTasksListModel.add(++pSelectedIndex,selectedObject);
            setSelectedIndex(pSelectedIndex);
        }
    }

    public TaskModel firstElement(){
        String label = (String) mTasksListModel.firstElement();
        return mLabel2Task.get(label);
    }

    public TaskModel lastElement(){
        String label = (String) mTasksListModel.lastElement();
        return mLabel2Task.get(label);
    }

    @Override
    public TaskModel[] getSelectedValues() {
        Object[] selectedValues = super.getSelectedValues();
        TaskModel[] tasks = new TaskModel[selectedValues.length];
        int i = 0;
        for (Object selectedValue : selectedValues) {
            tasks[i++] = mLabel2Task.get(selectedValue);
        }
        return tasks;

    }
}
