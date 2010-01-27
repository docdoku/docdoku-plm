package com.docdoku.client.ui.workflow;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JPanel;
import com.docdoku.core.entities.SerialActivityModel;
import com.docdoku.core.entities.TaskModel;


public abstract class SerialActivityModelCanvas extends JPanel{
    
    protected SerialActivityModel mActivityModel;

    public SerialActivityModelCanvas(SerialActivityModel pActivity) {
        mActivityModel=pActivity;
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
        
        java.util.List<TaskModel> tasks = mActivityModel.getTaskModels();
        Iterator<TaskModel> iti = tasks.iterator();
        while (iti.hasNext()) {
            constraints.weightx = 0;
            add(createTaskModel(iti.next()), constraints);
            constraints.gridx++;
            constraints.weightx = 1;
            if (iti.hasNext()){
                add(createHorizontalSeparator(), constraints);
                constraints.gridx++;
            }
            
        }
    }

    protected JComponent createTaskModel(TaskModel pTask){
        return new TaskModelCanvas(pTask);
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

    public SerialActivityModel getSerialActivityModel() {
        return mActivityModel;
    }
}
