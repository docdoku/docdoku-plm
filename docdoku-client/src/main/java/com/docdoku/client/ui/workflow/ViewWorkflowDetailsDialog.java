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

package com.docdoku.client.ui.workflow;

import com.docdoku.client.ui.common.CloseButton;

import javax.swing.*;
import java.awt.*;
import com.docdoku.client.localization.I18N;
import com.docdoku.core.workflow.Workflow;

public class ViewWorkflowDetailsDialog extends JDialog {
    
    private WorkflowCanvas mWorkflowCanvas;
    private JScrollPane mWorkflowScrollPane;
    private CloseButton mCloseButton;

    public ViewWorkflowDetailsDialog(Dialog pOwner, Workflow pWorkflow) {
        super(pOwner, I18N.BUNDLE.getString("ViewWorkflowDetails_title"), true);
        setLocationRelativeTo(pOwner);
        mWorkflowCanvas = new WorkflowCanvas(pWorkflow);
        mCloseButton = new CloseButton(this, I18N.BUNDLE.getString("Close_button"));
        mWorkflowScrollPane=new JScrollPane();
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mCloseButton);
        JPanel mainPanel = new JPanel(new BorderLayout());       
        mWorkflowScrollPane.getViewport().add(mWorkflowCanvas);
        mainPanel.add(mWorkflowScrollPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel();
        southPanel.add(mCloseButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }
}
