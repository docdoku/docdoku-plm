package com.docdoku.client.actions;

import com.docdoku.core.entities.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.core.entities.Task;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.approval.TaskDialog;
import com.docdoku.core.entities.TaskModel;

public class RejectAction extends ClientAbstractAction {
    
    public RejectAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Reject_title"), "/com/docdoku/client/resources/icons/trafficlight_red.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Reject_short_desc"));
        putValue(Action.LONG_DESCRIPTION,  I18N.BUNDLE.getString("Reject_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Reject_mnemonic_key")));
    }
    
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                TaskDialog source = (TaskDialog) pAE.getSource();
                Task task = source.getTask();
                String comment = source.getComment();
                MainController controller = MainController.getInstance();
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, I18N.BUNDLE.getString("Reject_question"), I18N.BUNDLE.getString("Confirm_label"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)){
                    try{
                        controller.reject(task.getKey(), comment);
                    } catch (Exception pEx) {
                        String message = pEx.getMessage()==null?I18N.BUNDLE
                                .getString("Error_unknown"):pEx.getMessage();
                        JOptionPane.showMessageDialog(null,
                                message, I18N.BUNDLE
                                .getString("Error_title"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    ExplorerFrame.unselectElementInAllFrame();
                }
            }
        };
        new TaskDialog(mOwner, I18N.BUNDLE.getString("RejectTask_title"), mdoc, action);
    }
}
