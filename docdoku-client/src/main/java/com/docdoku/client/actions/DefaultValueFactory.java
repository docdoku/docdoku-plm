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

package com.docdoku.client.actions;

import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.workflow.ParallelActivityModel;
import com.docdoku.core.workflow.SerialActivityModel;
import com.docdoku.core.workflow.TaskModel;
import com.docdoku.core.common.User;
import com.docdoku.core.workflow.WorkflowModel;
import com.docdoku.core.common.Workspace;


public class DefaultValueFactory {

    private DefaultValueFactory(){
        
    }
    
    public static WorkflowModel createDefaultWorkflowModel() {
        Workspace workspace=MainModel.getInstance().getWorkspace();
        String workflowId=I18N.BUNDLE.getString("DefaultWorkflowModel_title");
        User author=MainModel.getInstance().getUser();
        return new WorkflowModel(workspace,workflowId, author, getDefaultLifeCycleState());
    }

    public static SerialActivityModel createDefaultSerialActivityModel(WorkflowModel pWorkflowModel) {
        SerialActivityModel model = new SerialActivityModel(pWorkflowModel, getDefaultLifeCycleState());
        TaskModel task = new TaskModel(model,I18N.BUNDLE.getString("DefaultTask_title"), null, MainModel.getInstance().getUser());
        model.addTaskModel(task);
        return model;
    }

    public static ParallelActivityModel createDefaultParallelActivityModel(WorkflowModel pWorkflowModel) {
        ParallelActivityModel model =  new ParallelActivityModel(pWorkflowModel, getDefaultLifeCycleState(), 1);
        TaskModel task = new TaskModel(model, I18N.BUNDLE.getString("DefaultTask_title"), null, MainModel.getInstance().getUser());
        model.addTaskModel(task);
        return model;
    }

    public static String getDefaultLifeCycleState() {
        return I18N.BUNDLE.getString("DefaultState_title");
    }
}
