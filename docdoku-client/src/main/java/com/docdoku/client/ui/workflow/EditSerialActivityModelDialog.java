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

package com.docdoku.client.ui.workflow;

import com.docdoku.client.localization.I18N;
import com.docdoku.client.ui.common.OKCancelPanel;
import com.docdoku.core.workflow.SerialActivityModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditSerialActivityModelDialog  extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private EditSerialActivityModelPanel mActivityModelPanel;
    private ActionListener mAction;

    public EditSerialActivityModelDialog(Frame pOwner,SerialActivityModel pActivityModel,ActionListener pAddTaskAction, ActionListener pEditTaskAction, ActionListener pAction){
        super(pOwner,I18N.BUNDLE.getString("EditSerialActivity_title"),true);
        setLocationRelativeTo(pOwner);
        mActivityModelPanel=new EditSerialActivityModelPanel(pActivityModel,pAddTaskAction,pEditTaskAction);
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

    public void actionPerformed(ActionEvent e) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
