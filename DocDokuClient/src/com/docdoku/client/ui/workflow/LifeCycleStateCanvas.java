package com.docdoku.client.ui.workflow;

import com.docdoku.core.entities.Activity;
import com.docdoku.core.entities.ActivityModel;
import com.docdoku.core.entities.WorkflowModel;
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
