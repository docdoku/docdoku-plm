/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.client.ui.workflow;

import com.docdoku.core.workflow.ActivityModel;
import com.docdoku.core.workflow.ParallelActivityModel;
import com.docdoku.core.workflow.SerialActivityModel;
import com.docdoku.core.workflow.WorkflowModel;

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
