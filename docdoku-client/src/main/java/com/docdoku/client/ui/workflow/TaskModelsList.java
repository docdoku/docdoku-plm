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

package com.docdoku.client.ui.workflow;

import com.docdoku.core.workflow.TaskModel;

import java.lang.Integer;
import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;


public class TaskModelsList extends JList {

    private DefaultListModel mTasksListModel = new DefaultListModel();
    private LinkedList<TaskModel> mIndex2Task = new LinkedList<TaskModel>();

    public TaskModelsList() {
        setModel(mTasksListModel);
        setCellRenderer(new TaskListCellRenderer());
    }

    public TaskModelsList(Collection<TaskModel> pTasks) {
        this();
        setTasks(pTasks);
    }

    public void setTask(int pIndex, TaskModel pTask){
        String label=pTask.getTitle() + "/" + pTask.getWorker().getName();
        mIndex2Task.set(pIndex,pTask);
        mTasksListModel.set(pIndex,label);
    }

    public void setTasks(Collection<TaskModel> pTasks) {
        mTasksListModel.clear();
        mIndex2Task.clear();
        Iterator<TaskModel> iti = pTasks.iterator();
        while (iti.hasNext()) {
            addTask(iti.next());
        }
    }

    public void addTask(TaskModel pTask) {
        String label=pTask.getTitle() + "/" + pTask.getWorker().getName();
        mIndex2Task.add(pTask);
        mTasksListModel.addElement(label);
    }

    public void removeTask(TaskModel pTask) {
        int i = mIndex2Task.indexOf(pTask);
        mIndex2Task.remove(i);
        mTasksListModel.remove(i);
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
        int[] selectedIndices = super.getSelectedIndices();
        for (int selectedIndex : selectedIndices) {
            mIndex2Task.remove(selectedIndex);
            mTasksListModel.remove(selectedIndex);
        }
    }

    public void moveUpTaskModel(int pSelectedIndex) {
        if (pSelectedIndex > 0) {
            Object selectedObject = mTasksListModel.remove(pSelectedIndex);
            mTasksListModel.add(pSelectedIndex-1,selectedObject);
            setSelectedIndex(pSelectedIndex-1);
            
            TaskModel selectedTask = mIndex2Task.remove(pSelectedIndex);
            mIndex2Task.add(pSelectedIndex-1,selectedTask);
        }
    }

    public void moveDownTaskModel(int pSelectedIndex) {
        if (pSelectedIndex < mTasksListModel.size() - 1) {
            Object selectedObject = mTasksListModel.remove(pSelectedIndex);
            mTasksListModel.add(pSelectedIndex+1,selectedObject);
            setSelectedIndex(pSelectedIndex+1);
            
            TaskModel selectedTask = mIndex2Task.remove(pSelectedIndex);
            mIndex2Task.add(pSelectedIndex+1,selectedTask);
        }
    }

    public TaskModel firstElement(){
        return mIndex2Task.peekFirst();
    }

    public TaskModel lastElement(){
        return mIndex2Task.peekLast();
    }

    @Override
    public TaskModel[] getSelectedValues() {
        int[] selectedIndices = super.getSelectedIndices();
        TaskModel[] tasks = new TaskModel[selectedIndices.length];
        int i = 0;
        for (int selectedIndex : selectedIndices) {
            tasks[i++] = mIndex2Task.get(selectedIndex);
        }
        return tasks;

    }
}
