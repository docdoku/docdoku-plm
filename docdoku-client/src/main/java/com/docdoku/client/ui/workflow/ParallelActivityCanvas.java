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
import com.docdoku.core.workflow.TaskModel;
import java.awt.*;
import java.util.Iterator;
import javax.swing.*;

import com.docdoku.core.workflow.ParallelActivity;
import com.docdoku.core.workflow.Task;



public class ParallelActivityCanvas extends JPanel{
    
    private ParallelActivity mActivity;
    
    public ParallelActivityCanvas(ParallelActivity pActivity) {
        mActivity=pActivity;
        createLayout();
    }
    
    protected void createLayout(){
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.weightx = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        constraints.gridx = 0;
        constraints.gridy = getYCenter();
        
        constraints.weighty = 0;
        add(new JLabel(mActivity.getTasksToComplete()+"/"+mActivity.getTasks().size()), constraints);
        
        constraints.gridx++;
        constraints.gridy = 0;

        Iterator<Task> ite = mActivity.getTasks().iterator();
        
        constraints.fill = GridBagConstraints.VERTICAL;
        
        Task task = ite.next();
        add(createTask(task), constraints);
        
        for (int taskNumber = 1; ite.hasNext(); taskNumber++) {
            constraints.gridy++;
            constraints.weighty = 1;
            add(createVerticalSeparator(), constraints);
            constraints.gridy++;
            constraints.weighty = 0;
            
            if ((mActivity.getTasks().size() % 2 == 0) && (mActivity.getTasks().size() / 2 == taskNumber)) {
                constraints.fill = GridBagConstraints.BOTH;
                constraints.insets = new Insets(4, 2, 4, 2);
                add(createVerticalSeparator(), constraints);
                constraints.insets = new Insets(2, 2, 2, 2);
                constraints.fill = GridBagConstraints.VERTICAL;
            } else {
                task = ite.next();
                add(createTask(task), constraints);
            }
        }
    }
    
    
    protected JComponent createVerticalSeparator() {
        JComponent verticalSeparator = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                int width = getWidth();
                int height = getHeight();
                g.drawLine(width / 2, 0, width / 2, height);
            }
        };
        verticalSeparator.setPreferredSize(new Dimension(10, 10));
        return verticalSeparator;
    }
    
    protected int getYCenter(){
        return (mActivity.getTasks().size()/2)*2;       
    }
    
    protected JComponent createTask(Task pTask){
        TaskModel taskModel = new TaskModel();
        taskModel.setNum(pTask.getNum());
        taskModel.setTitle(pTask.getTitle());
        taskModel.setInstructions(pTask.getInstructions());
        taskModel.setWorker(pTask.getWorker());
        JComponent canvas=new TaskModelCanvas(taskModel);
        boolean rejected=(pTask.isRejected());
        boolean approved=(pTask.isApproved());
        canvas.setToolTipText("<html>"+I18N.BUNDLE.getString("Comment_column_label")+" : "+pTask.getClosureComment()+"<br>"+I18N.BUNDLE.getString("Date_column_label")+" : "+pTask.getClosureDate()+"<br>"+I18N.BUNDLE.getString("Iteration_column_label")+" : "+pTask.getTargetIteration());
        if(rejected)
            canvas.setBorder(BorderFactory.createLineBorder(Color.RED,3));
        else if(approved)
            canvas.setBorder(BorderFactory.createLineBorder(Color.GREEN,3));
        return canvas;
    }
    
}
