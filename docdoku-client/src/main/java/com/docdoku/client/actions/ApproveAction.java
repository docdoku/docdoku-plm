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

import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.Task;
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
        DocumentMaster docM = mOwner.getSelectedDocM();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                TaskDialog source = (TaskDialog) pAE.getSource();
                Task task = source.getTask();
                DocumentMaster docM = source.getDocM();
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
        new TaskDialog(mOwner, I18N.BUNDLE.getString("ApproveTask_title"), docM, action);
    }
}
