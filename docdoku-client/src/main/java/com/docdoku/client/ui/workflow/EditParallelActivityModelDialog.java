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

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.workflow.ParallelActivityModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditParallelActivityModelDialog extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private EditParallelActivityModelPanel mActivityModelPanel;
    private ActionListener mAction;

    public EditParallelActivityModelDialog(Frame pOwner,ParallelActivityModel pActivityModel, ActionListener pAddTaskAction, ActionListener pEditTaskAction, ActionListener pAction){
        super(pOwner,I18N.BUNDLE.getString("EditParallelActivity_title"),true);
        setLocationRelativeTo(pOwner);
        mActivityModelPanel=new EditParallelActivityModelPanel(pActivityModel, pAddTaskAction, pEditTaskAction);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction=pAction;
        createLayout();
        setVisible(true);
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(mActivityModelPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public int getNumberOfNeededCompletedTasks() {
        return mActivityModelPanel.getNumberOfNeededCompletedTasks();
    }

    public void actionPerformed(ActionEvent e) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
