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

package com.docdoku.client.actions;

import com.docdoku.client.ui.workflow.EditSerialActivityModelDialog;
import com.docdoku.client.ui.workflow.EditableSerialActivityModelCanvas;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;
import com.docdoku.core.workflow.SerialActivityModel;
import com.docdoku.core.workflow.WorkflowModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditSerialActivityModelActionListener implements ActionListener {


    public void actionPerformed(ActionEvent pAE) {
        final EditableSerialActivityModelCanvas canvas = (EditableSerialActivityModelCanvas) pAE.getSource();
        final WorkflowModelFrame owner = (WorkflowModelFrame) SwingUtilities.getAncestorOfClass(WorkflowModelFrame.class, canvas);
        //clone the object in case the user cancels the action
        final SerialActivityModel clonedActivityModel = canvas.getSerialActivityModel().clone();
        ActionListener action = new ActionListener() {
            public void actionPerformed(ActionEvent pAE) {
                WorkflowModel model = owner.getWorkflowModel();
                int step = canvas.getSerialActivityModel().getStep();
                model.setActivityModel(step,clonedActivityModel);
                canvas.setSerialActivityModel(clonedActivityModel);
                canvas.refresh();
            }
        };
        new EditSerialActivityModelDialog(owner, clonedActivityModel, new CreateTaskActionListener(), new EditTaskActionListener(), action);
    }

}