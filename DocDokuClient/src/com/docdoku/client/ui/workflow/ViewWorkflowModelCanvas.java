package com.docdoku.client.ui.workflow;

import com.docdoku.core.*;
import com.docdoku.core.entities.ActivityModel;
import com.docdoku.core.entities.ParallelActivityModel;
import com.docdoku.core.entities.SerialActivityModel;
import com.docdoku.core.entities.WorkflowModel;

import javax.swing.*;


public class ViewWorkflowModelCanvas extends WorkflowModelCanvas {


    public ViewWorkflowModelCanvas(WorkflowModel pWorkflowModel) {
        super(pWorkflowModel);
        createLayout();
    }

    protected JComponent createActivityModel(int pStep){
        ActivityModel activityModel = mWorkflowModel.getActivityModel(pStep);
        if (activityModel instanceof SerialActivityModel) {
            return createSerialActivityModel((SerialActivityModel)activityModel);
        } else if (activityModel instanceof ParallelActivityModel) {
            return createParallelActivityModel((ParallelActivityModel)activityModel, pStep);
        } else
            throw new RuntimeException("Unexpected error: unrecognized activity type");
    }

    private JComponent createParallelActivityModel(ParallelActivityModel pActivityModel, int pStep) {
        return new ViewParallelActivityModelCanvas(pActivityModel);
    }

    private JComponent createSerialActivityModel(SerialActivityModel pActivityModel) {
        return new ViewSerialActivityModelCanvas(pActivityModel);
    }
}
