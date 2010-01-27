package com.docdoku.client.actions;

import com.docdoku.core.*;
import com.docdoku.client.data.MainModel;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.ActivityModel;
import com.docdoku.core.entities.ParallelActivityModel;
import com.docdoku.core.entities.SerialActivityModel;
import com.docdoku.core.entities.TaskModel;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.WorkflowModel;
import com.docdoku.core.entities.Workspace;
import java.util.HashMap;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;

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
