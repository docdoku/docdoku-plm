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

import com.docdoku.client.ui.ExplorerFrame;
import com.docdoku.client.ui.workflow.WorkflowModelFrame;

import javax.swing.*;
import com.docdoku.client.localization.I18N;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;


public class CreateWorkflowModelAction extends ClientAbstractAction {

    public CreateWorkflowModelAction(ExplorerFrame pOwner) {
        super(I18N.BUNDLE.getString("Workflow_title"), "/com/docdoku/client/resources/icons/branch_element_new.png", pOwner);
        putValue(Action.SHORT_DESCRIPTION,I18N.BUNDLE.getString("Workflow_short_desc"));
        putValue(Action.LONG_DESCRIPTION,I18N.BUNDLE.getString("Workflow_long_desc"));
        putValue(Action.MNEMONIC_KEY, new Integer(I18N.getCharBundle("Workflow_mnemonic_key")));
    }

    public void actionPerformed(ActionEvent pAE) {
        final ActionListener saveAsWorkflowModelAction = new SaveAsWorkflowModelActionListener();
        final ActionListener editParallelActivityModelAction = new EditParallelActivityModelActionListener();
        final ActionListener editSerialActivityModelAction = new EditSerialActivityModelActionListener();
        final ActionListener deleteParallelActivityModelAction = new DeleteParallelActivityModelActionListener();
        final ActionListener deleteSerialActivityModelAction = new DeleteSerialActivityModelActionListener();
        final ActionListener editLifeCycleStateAction = new EditLifeCycleStateActionListener();
        final MouseListener horizontalSeparatorMouseListener = new HorizontalSeparatorMouseListener();
        new WorkflowModelFrame(DefaultValueFactory.createDefaultWorkflowModel(), saveAsWorkflowModelAction, editParallelActivityModelAction, editSerialActivityModelAction, deleteParallelActivityModelAction, deleteSerialActivityModelAction, editLifeCycleStateAction, horizontalSeparatorMouseListener);
    }
}
