package com.docdoku.client.actions;

import com.docdoku.core.entities.MasterDocument;
import com.docdoku.core.entities.Task;
import com.docdoku.client.ui.ExplorerFrame;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.approval.TaskDialog;

public class ApproveAction extends ClientAbstractAction {
    
    public ApproveAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Approve_title"), "/com/docdoku/client/resources/icons/trafficlight_green.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION, I18N.BUNDLE.getString("Approve_short_desc"));
        putValue(Action.LONG_DESCRIPTION, I18N.BUNDLE.getString("Approve_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Approve_mnemonic_key")));
    }
    
    public void actionPerformed(ActionEvent pAE) {
        MasterDocument mdoc = mOwner.getSelectedMDoc();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                TaskDialog source = (TaskDialog) pAE.getSource();
                Task task = source.getTask();
                MasterDocument mdoc = source.getMDoc();
                String comment = source.getComment();
                MainController controller = MainController.getInstance();
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mOwner, I18N.BUNDLE.getString("Approve_question"), I18N.BUNDLE.getString("Confirm_label"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)){
                    try{
                        controller.approve(task.getKey(),comment);
                    }catch (Exception pEx) {
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
        new TaskDialog(mOwner, I18N.BUNDLE.getString("ApproveTask_title"), mdoc, action);
    }
}
