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

import com.docdoku.core.entities.ActivityModel;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;
import java.awt.*;


public abstract class WorkflowModelCanvas extends JPanel {
    
    protected WorkflowModel mWorkflowModel;
    private final static ImageIcon START_FLAG_ICON;
    private final static ImageIcon END_FLAG_ICON;

    static {
        Image img = Toolkit.getDefaultToolkit().getImage(WorkflowModelCanvas.class.getResource("/com/docdoku/client/resources/icons/flag_green.png"));
        START_FLAG_ICON = new ImageIcon(img);
        img = Toolkit.getDefaultToolkit().getImage(WorkflowModelCanvas.class.getResource("/com/docdoku/client/resources/icons/target.png"));
        END_FLAG_ICON = new ImageIcon(img);
    }
    
    public WorkflowModelCanvas(WorkflowModel pWorkflowModel) {
        mWorkflowModel = pWorkflowModel;
    }
    
    protected void createLayout() {
        setLayout(new GridBagLayout());
        layoutStartFlag();
        layoutTransition(0);
        for (int i = 0; i < mWorkflowModel.numberOfSteps(); i++) {          
            layoutActivityModel(i);
            layoutTransition(i+1);
        }
        layoutEndFlag();
        layoutLifeCycle(mWorkflowModel);
    }
    
    private void layoutLifeCycle(WorkflowModel pWorkflowModel) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets=new Insets(5, 0, 5, 0);
        constraints.gridy = 1;
        constraints.gridx = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        for (ActivityModel activityModel:pWorkflowModel.getActivityModels()) {
            constraints.weightx = 0;
            add(createLifeCycleState(activityModel), constraints);
            constraints.gridx = GridBagConstraints.RELATIVE;
            constraints.weightx = 1;
            add(createLifeCycleSeparator(),constraints);
        }
        constraints.weightx = 0;
        add(createFinalLifeCycleState(pWorkflowModel), constraints);
    }

    
    private void layoutActivityModel(int pStep) {
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
        add(createHorizontalSeparator(pRank), constraints);
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

    private JComponent createLifeCycleSeparator() {
        return new JLabel();
    }

    protected JComponent createHorizontalSeparator(int pRank) {
        return new HorizontalSeparatorCanvas(pRank);
    }

    protected JComponent createLifeCycleState(ActivityModel pActivityModel) {
        return new LifeCycleStateCanvas(pActivityModel.getWorkflowModel(),pActivityModel.getStep());
    }

    protected JComponent createFinalLifeCycleState(WorkflowModel pWorkflowModel) {
        return new LifeCycleStateCanvas(pWorkflowModel);
    }
    
    protected abstract JComponent createActivityModel(int pStep);
}
