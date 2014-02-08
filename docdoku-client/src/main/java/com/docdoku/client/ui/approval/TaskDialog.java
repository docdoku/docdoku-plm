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

package com.docdoku.client.ui.approval;

import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.document.DocumentMaster;
import com.docdoku.core.workflow.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

public class TaskDialog extends JDialog implements ActionListener {
    private TaskPanel mTaskPanel;
    private OKCancelPanel mOKCancelPanel;
    private ActionListener mAction;
    private DocumentMaster mDocM;

    public TaskDialog(Frame pOwner, String pTitle, DocumentMaster pDocM, ActionListener pAction) {
        super(pOwner, pTitle, true);
        setLocationRelativeTo(pOwner);
        Collection<Task> runningTasks=pDocM.getWorkflow().getRunningTasks();
        mTaskPanel=new TaskPanel(runningTasks);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction = pAction;
        mDocM=pDocM;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(mTaskPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public Task getTask() {
        return mTaskPanel.getTask();
    }

    public DocumentMaster getDocM() {
        return mDocM;
    }
    
    public String getComment() {
        return mTaskPanel.getComment();
    }

    public void actionPerformed(ActionEvent pAE) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
