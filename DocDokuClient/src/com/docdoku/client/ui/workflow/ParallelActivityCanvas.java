package com.docdoku.client.ui.workflow;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.TaskModel;
import java.awt.Color;
import java.awt.*;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.*;

import com.docdoku.core.entities.ParallelActivity;
import com.docdoku.core.entities.Task;



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
