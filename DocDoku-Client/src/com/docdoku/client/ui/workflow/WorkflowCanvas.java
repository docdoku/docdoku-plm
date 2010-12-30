/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.*;
import com.docdoku.core.entities.Activity;
import com.docdoku.core.entities.ParallelActivity;
import com.docdoku.core.entities.SerialActivity;
import com.docdoku.core.entities.Workflow;



public class WorkflowCanvas extends JPanel {
    
    private Workflow mWorkflow;
    private final static ImageIcon START_FLAG_ICON;
    private final static ImageIcon END_FLAG_ICON;
    
    static {
        Image img = Toolkit.getDefaultToolkit().getImage(WorkflowCanvas.class.getResource("/com/docdoku/client/resources/icons/flag_green.png"));
        START_FLAG_ICON = new ImageIcon(img);
        img = Toolkit.getDefaultToolkit().getImage(WorkflowCanvas.class.getResource("/com/docdoku/client/resources/icons/target.png"));
        END_FLAG_ICON = new ImageIcon(img);
    }
    public WorkflowCanvas(Workflow pWorkflow) {
        mWorkflow=pWorkflow;
        createLayout();
    }
    
    protected void createLayout() {
        setLayout(new GridBagLayout());
        layoutStartFlag();
        layoutTransition(0);
        for (int i = 0; i < mWorkflow.numberOfSteps(); i++) {
            layoutActivity(i);
            layoutTransition(i+1);
        }
        layoutEndFlag();
        layoutLifeCycle(mWorkflow);
    }
    
    private void layoutLifeCycle(Workflow pWorkflow) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets=new Insets(5, 0, 5, 0);
        constraints.gridy = 1;
        constraints.gridx = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        for (Activity activity:pWorkflow.getActivities()) {
            constraints.weightx = 0;
            add(new LifeCycleStateCanvas(activity.getLifeCycleState()), constraints);
            constraints.gridx = GridBagConstraints.RELATIVE;
            constraints.weightx = 1;
            add(new JLabel(),constraints);
        }
        constraints.weightx = 0;
        add(new LifeCycleStateCanvas(pWorkflow.getFinalLifeCycleState()), constraints);
    }
    
    private void layoutActivity(int pStep) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx=GridBagConstraints.RELATIVE;
        constraints.insets=new Insets(5, 0, 5, 0);
        add(createActivityModel(pStep),constraints);
    }
    
    private void layoutTransition(int pRank) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = GridBagConstraints.RELATIVE;
        add(new HorizontalSeparatorCanvas(pRank), constraints);
    }
    
    private void layoutStartFlag() {
        JLabel startFlag=new JLabel(START_FLAG_ICON);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.insets=new Insets(5, 5, 5, 0);
        add(startFlag, constraints);
    }
    
    private void layoutEndFlag() {
        JLabel endFlag=new JLabel(END_FLAG_ICON);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.insets=new Insets(5, 0, 5, 5);
        add(endFlag, constraints);
    }
    
    protected JComponent createActivityModel(int pStep){
        Activity activity = mWorkflow.getActivity(pStep);
        if (activity instanceof SerialActivity) {
            return createSerialActivity((SerialActivity)activity, pStep);
        } else if (activity instanceof ParallelActivity) {
            return createParallelActivity((ParallelActivity)activity, pStep);
        } else
            throw new RuntimeException("Unexpected error: unrecognized activity type");
    }
    
    private JComponent createParallelActivity(ParallelActivity pActivity, int pStep) {
        return new ParallelActivityCanvas(pActivity);
    }
    
    private JComponent createSerialActivity(SerialActivity pActivity,int pStep) {
        return new SerialActivityCanvas(pActivity);
    }
    
}
