package com.docdoku.client.ui.workflow;


import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.TaskModel;
import java.awt.Color;
import com.docdoku.core.entities.SerialActivity;
import com.docdoku.core.entities.Task;
import java.awt.*;
import java.util.Iterator;

import javax.swing.*;


public class SerialActivityCanvas extends JPanel {
    
    private SerialActivity mActivity;
    
    public SerialActivityCanvas(SerialActivity pActivity) {
        mActivity=pActivity;
        createLayout();
    }
    
    protected void createLayout(){
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.weighty = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 0;
        
        constraints.weightx = 0;
        
        Iterator<Task> ite=mActivity.getTasks().iterator();
        while (ite.hasNext()){
            Task task=ite.next();
            constraints.weightx = 0;
            add(createTask(task), constraints);

            constraints.gridx++;
            constraints.weightx = 1;
            if (ite.hasNext()){
                add(createHorizontalSeparator(), constraints);
                constraints.gridx++;
            }
            
        }
    }
    
    protected JComponent createHorizontalSeparator() {
        JComponent horizontalSeparator = new JComponent() {
            protected void paintComponent(Graphics g) {
                int width = getWidth();
                int height = getHeight();
                g.drawRect(0, 0, width, height);
            }
        };
        horizontalSeparator.setPreferredSize(new Dimension(10, 1));
        return horizontalSeparator;
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
