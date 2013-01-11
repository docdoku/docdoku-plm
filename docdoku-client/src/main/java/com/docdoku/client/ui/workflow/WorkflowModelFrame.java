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


import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.workflow.WorkflowModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;


public class WorkflowModelFrame extends JFrame implements ActionListener{

    private WorkflowModelToolBar mToolBar;
    private JPanel mCanvas;
    private JScrollPane mWorkflowScrollPane;
    private WorkflowModel mWorkflowModel;
    private ActionListener mSaveAsWorkflowModelAction;

    public WorkflowModelFrame(WorkflowModel pWorkflowModel, ActionListener pSaveAsWorkflowModelAction, ActionListener pEditParallelActivityModelAction,ActionListener pEditSerialActivityModelAction, ActionListener pDeleteParallelActivityModelAction, ActionListener pDeleteSerialActivityModelAction, ActionListener pEditLifeCycleStateAction,MouseListener pHorizontalSeparatorMouseListener) {
        super(pWorkflowModel.getId());
        setLocationRelativeTo(null);
        mWorkflowModel=pWorkflowModel;
        mToolBar=new WorkflowModelToolBar(this);
        mWorkflowScrollPane=new JScrollPane();
        mCanvas = new EditableWorkflowModelCanvas(mWorkflowModel,pEditParallelActivityModelAction,pEditSerialActivityModelAction,pDeleteParallelActivityModelAction, pDeleteSerialActivityModelAction, pEditLifeCycleStateAction, pHorizontalSeparatorMouseListener);
        mSaveAsWorkflowModelAction=pSaveAsWorkflowModelAction;
        createLayout();
        createListener();
        setVisible(true);
    }
    
    public WorkflowModelToolBar.BehaviorMode getBehaviorMode(){
        return mToolBar.getSelectedMode();
    }
    
    private void createLayout() {
        Border border = BorderFactory.createEtchedBorder();
        mCanvas.setBorder(new CompoundBorder(border, GUIConstants.WORKFLOW_CANVAS_MARGIN_BORDER));

        mWorkflowScrollPane.getViewport().add(mCanvas);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mWorkflowScrollPane, BorderLayout.CENTER);
        getContentPane().add(mToolBar, BorderLayout.WEST);
        pack();
    }

    private void createListener() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public WorkflowModel getWorkflowModel(){
        return mWorkflowModel;
    }

    public void actionPerformed(ActionEvent e) {
        mSaveAsWorkflowModelAction.actionPerformed(new ActionEvent(this,0,null));
    }

}