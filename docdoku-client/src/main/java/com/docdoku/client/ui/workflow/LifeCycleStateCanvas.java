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

import com.docdoku.core.workflow.WorkflowModel;
import javax.swing.*;
import java.awt.*;


public class LifeCycleStateCanvas extends JPanel{

    private JLabel mStateLabel;
    private int mStep;
    private WorkflowModel mWorkflowModel;
    
    
    public LifeCycleStateCanvas(WorkflowModel pWorkflowModel, int pStep){
        mWorkflowModel=pWorkflowModel;
        mStep=pStep;
        mStateLabel=new JLabel(mWorkflowModel.getLifeCycle().get(pStep),SwingConstants.CENTER);
        createLayout();
    }

    public LifeCycleStateCanvas(WorkflowModel pWorkflowModel){
        mWorkflowModel=pWorkflowModel;
        mStep=mWorkflowModel.numberOfSteps();
        mStateLabel=new JLabel(mWorkflowModel.getFinalLifeCycleState(),SwingConstants.CENTER);
        createLayout();
    }
    
    public LifeCycleStateCanvas(String pState){
        mStateLabel=new JLabel(pState,SwingConstants.CENTER);
        createLayout();
    }
    
    public void setState(String pState){
        mStateLabel.setText(pState);
    }

    public String getState(){
        return mStateLabel.getText();
    }

    public int getStep() {
        return mStep;
    }
    
    public WorkflowModel getWorkflowModel() {
        return mWorkflowModel;
    }
            

    private void createLayout(){
        setBackground(Color.GRAY);
        setBorder(BorderFactory.createEtchedBorder());
        mStateLabel.setForeground(Color.WHITE);
        add(mStateLabel);
    }
}
