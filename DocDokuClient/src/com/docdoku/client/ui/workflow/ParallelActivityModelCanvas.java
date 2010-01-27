package com.docdoku.client.ui.workflow;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.docdoku.core.entities.ParallelActivityModel;
import com.docdoku.core.entities.TaskModel;


public abstract class ParallelActivityModelCanvas extends JPanel{

    protected ParallelActivityModel mActivityModel;

    public ParallelActivityModelCanvas(ParallelActivityModel pActivity) {
        mActivityModel=pActivity;
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
        add(new JLabel(mActivityModel.getTasksToComplete()+"/"+mActivityModel.getTaskModels().size()), constraints);
        
        constraints.gridx++;
        constraints.gridy = 0;
        Collection<TaskModel> tasks = mActivityModel.getTaskModels();
        Iterator<TaskModel> iti = tasks.iterator();
        
        constraints.fill = GridBagConstraints.VERTICAL;
        add(createTaskModel(iti.next()), constraints);
        for (int taskNumber = 1; iti.hasNext(); taskNumber++) {
            constraints.gridy++;
            constraints.weighty = 1;
            add(createVerticalSeparator(), constraints);
            constraints.gridy++;
            constraints.weighty = 0;
            
            if ((tasks.size() % 2 == 0) && (tasks.size() / 2 == taskNumber)) {
                constraints.fill = GridBagConstraints.BOTH;
                constraints.insets = new Insets(4, 2, 4, 2);
               add(createVerticalSeparator(), constraints);
               constraints.insets = new Insets(2, 2, 2, 2);
                constraints.fill = GridBagConstraints.VERTICAL;
            } else {
                add(createTaskModel(iti.next()), constraints);
            }
        }
    }

    protected JComponent createTaskModel(TaskModel pTaskModel){
        return new TaskModelCanvas(pTaskModel);
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
        return (mActivityModel.getTaskModels().size()/2)*2;       
    }

    public ParallelActivityModel getParalleActivityModel() {
        return mActivityModel;
    }
    
}
