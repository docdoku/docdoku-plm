package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import java.awt.*;
import java.awt.event.ActionListener;


public class CreateTaskDialog extends TaskModelDialog{

    public CreateTaskDialog(Frame pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("TaskCreation_title"));
        init(pAction);
    }

    public CreateTaskDialog(Dialog pOwner, ActionListener pAction) {
        super(pOwner, I18N.BUNDLE.getString("TaskCreation_title"));
        init(pAction);
    }



    protected void init(ActionListener pAction){
        mEditTaskModelPanel = new EditTaskModelPanel();
        super.init(pAction);
        mOKCancelPanel.setEnabled(false);
        setVisible(true);
    }
}
