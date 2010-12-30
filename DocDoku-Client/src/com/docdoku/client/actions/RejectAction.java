/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

import com.docdoku.core.entities.MasterDocument;
import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.core.entities.Task;

import javax.swing.*;

import java.awt.event.*;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.approval.TaskDialog;

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
