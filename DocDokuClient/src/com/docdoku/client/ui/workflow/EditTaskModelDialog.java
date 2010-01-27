package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.core.entities.TaskModel;

import java.awt.*;
import java.awt.event.ActionListener;


public class EditTaskModelDialog extends TaskModelDialog{

    private TaskModel mTaskModel;

    public EditTaskModelDialog(Frame pOwner, ActionListener pAction, TaskModel pTaskModel) {
        super(pOwner, I18N.BUNDLE.getString("EditTask_title"));
        init(pAction, pTaskModel);
    }

    public EditTaskModelDialog(Dialog pOwner, ActionListener pAction, TaskModel pTaskModel) {
        super(pOwner, I18N.BUNDLE.getString("EditTask_title"));
        init(pAction, pTaskModel);
    }

    protected void init(ActionListener pAction, TaskModel pTaskModel){
        mTaskModel=pTaskModel;
        mEditTaskModelPanel = new EditTaskModelPanel(mTaskModel);
        super.init(pAction);
        setVisible(true);
    }

    public TaskModel getTaskModel(){
        return mTaskModel;
    }
}
