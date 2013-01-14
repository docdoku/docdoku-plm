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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class EditLifeCycleStateDialog extends JDialog implements ActionListener {

    private OKCancelPanel mOKCancelPanel;
    private EditLifeCycleStatePanel mLifeCycleStatePanel;
    private ActionListener mAction;

    public EditLifeCycleStateDialog(Frame pOwner,String pState,ActionListener pAction){
        super(pOwner,I18N.BUNDLE.getString("EditLifecycle_title"),true);
        setLocationRelativeTo(pOwner);
        mLifeCycleStatePanel=new EditLifeCycleStatePanel(pState);
        mOKCancelPanel = new OKCancelPanel(this, this);
        mAction=pAction;
        createLayout();
        setVisible(true);
    }

    public String getState(){
        return mLifeCycleStatePanel.getState();
    }

    private void createLayout() {
        getRootPane().setDefaultButton(mOKCancelPanel.getOKButton());
        JPanel mainPanel = new JPanel(new BorderLayout());
        Box centerPanel = new Box(BoxLayout.Y_AXIS);
        centerPanel.add(mLifeCycleStatePanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(mOKCancelPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        mAction.actionPerformed(new ActionEvent(this, 0, null));
    }
}
