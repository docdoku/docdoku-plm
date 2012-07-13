/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
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

import com.docdoku.core.workflow.ParallelActivityModel;
import com.docdoku.core.workflow.SerialActivityModel;
import com.docdoku.core.workflow.WorkflowModel;
import java.awt.Cursor;

import javax.swing.*;
import java.awt.event.*;
import com.docdoku.core.workflow.ActivityModel;



public class EditableWorkflowModelCanvas extends WorkflowModelCanvas {
    
    private ActionListener mEditParallelActivityModelAction;
    private ActionListener mEditSerialActivityModelAction;
    private ActionListener mDeleteParallelActivityModelAction;
    private ActionListener mDeleteSerialActivityModelAction;
    private ActionListener mEditLifeCycleStateAction;
    private MouseListener mHorizontalSeparatorMouseListener;
    
    public EditableWorkflowModelCanvas(WorkflowModel pWorkflowModel, ActionListener pEditParallelActivityModelAction, ActionListener pEditSerialActivityModelAction, ActionListener pDeleteParallelActivityModelAction, ActionListener pDeleteSerialActivityModelAction, ActionListener pEditLifeCycleStateAction, MouseListener pHorizontalSeparatorMouseListener) {
        super(pWorkflowModel);
        mEditParallelActivityModelAction = pEditParallelActivityModelAction;
        mEditSerialActivityModelAction = pEditSerialActivityModelAction;
        mDeleteParallelActivityModelAction = pDeleteParallelActivityModelAction;
        mDeleteSerialActivityModelAction = pDeleteSerialActivityModelAction;
        mEditLifeCycleStateAction = pEditLifeCycleStateAction;
        mHorizontalSeparatorMouseListener = pHorizontalSeparatorMouseListener;
        createLayout();
    }
    
    public void refresh() {
        removeAll();
        createLayout();
        revalidate();
        repaint();
    }
    
    @Override
    protected JComponent createHorizontalSeparator(int pRank) {
        final JComponent component = super.createHorizontalSeparator(pRank);
        component.addMouseListener(mHorizontalSeparatorMouseListener);
        return component;
    }
    
    protected JComponent createActivityModel(int pStep){
        ActivityModel activityModel = mWorkflowModel.getActivityModel(pStep);
        if (activityModel instanceof SerialActivityModel) {
            return createSerialActivityModel((SerialActivityModel)activityModel);
        } else if (activityModel instanceof ParallelActivityModel) {
            return createParallelActivityModel((ParallelActivityModel)activityModel);
        } else
            throw new RuntimeException("Unexpected error: unrecognized activity type");
    }
    
    private JComponent createParallelActivityModel(ParallelActivityModel pActivityModel) {
        final JComponent component = new EditableParallelActivityModelCanvas(pActivityModel);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent pME) {
                if (pME.getClickCount() > 1) {
                    mEditParallelActivityModelAction.actionPerformed(new ActionEvent(component, 0, null));
                }
            }
        });
        component.setComponentPopupMenu(new ActivityModelPopupMenu(new ActionListener(){
            public void actionPerformed(ActionEvent pAE){
                mEditParallelActivityModelAction.actionPerformed(new ActionEvent(component, 0, null));
            }
        },new ActionListener(){
            public void actionPerformed(ActionEvent pAE){
                mDeleteParallelActivityModelAction.actionPerformed(new ActionEvent(component, 0, null));
            }
        }));
        return component;
    }
    
    private JComponent createSerialActivityModel(SerialActivityModel pActivityModel) {
        final JComponent component = new EditableSerialActivityModelCanvas(pActivityModel);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent pME) {
                if (pME.getClickCount() > 1) {
                    mEditSerialActivityModelAction.actionPerformed(new ActionEvent(component, 0, null));
                }
            }
        });
        component.setComponentPopupMenu(new ActivityModelPopupMenu(new ActionListener(){
            public void actionPerformed(ActionEvent pAE){
                mEditSerialActivityModelAction.actionPerformed(new ActionEvent(component, 0, null));
            }
        },new ActionListener(){
            public void actionPerformed(ActionEvent pAE){
                mDeleteSerialActivityModelAction.actionPerformed(new ActionEvent(component, 0, null));
            }
        }));
        return component;
    }

    @Override
    protected JComponent createLifeCycleState(ActivityModel pActivityModel) {
        final JComponent component = super.createLifeCycleState(pActivityModel);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent pME) {
                mEditLifeCycleStateAction.actionPerformed(new ActionEvent(component, 0, null));
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                component.setCursor(Cursor.getDefaultCursor());
            }
        });
        return component;
    }
    
    @Override
    protected JComponent createFinalLifeCycleState(WorkflowModel pWorkflowModel) {
        final JComponent component = super.createFinalLifeCycleState(pWorkflowModel);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent pME) {
                mEditLifeCycleStateAction.actionPerformed(new ActionEvent(component, 0, null));
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                component.setCursor(Cursor.getDefaultCursor());
            }
        });
        return component;
    }
}
