package com.docdoku.client.ui.workflow;


import com.docdoku.client.ui.common.GUIConstants;
import com.docdoku.core.entities.WorkflowModel;

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