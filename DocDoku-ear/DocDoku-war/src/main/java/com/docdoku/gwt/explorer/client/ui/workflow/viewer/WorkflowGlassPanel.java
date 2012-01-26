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

package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.ui.doc.DocMainPanel;
import com.docdoku.gwt.explorer.client.ui.workflow.viewer.TaskChangeEvent.Type;
import com.docdoku.gwt.explorer.shared.DocumentMasterDTO;
import com.docdoku.gwt.explorer.shared.WorkflowDTO;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 *
 * @author Emmanuel Nhan
 */
public class WorkflowGlassPanel extends PopupPanel implements TaskListener {

    public final static double RATIO = 0.85;
    // note : this is a scroll size experimentaly build with Firefox & may not fit other browsers
    private final static int SCROLL_SIZE = 15;
    private WorkflowViewer viewer;
    private ScrollPanel scroll;
    private WorkflowDTO workflow;
    private String visitorName;
    private DocMainPanel parentPanel;
    private Action approveCommand;
    private Action rejectCommand;

    public WorkflowGlassPanel(DocMainPanel owner) {
        super(true);
        setGlassEnabled(true);
        parentPanel = owner;
    }

    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
        this.visitorName = ExplorerConstants.getInstance().getUser().getName();
        viewer = new WorkflowViewer(workflow, visitorName, this);
        createLayout();
    }
     
    private void callApprove(int activityStep, int taskStep, String comment) {
        approveCommand.execute(workflow.getWorkspaceId(), workflow.getId(), activityStep, taskStep, comment, this);
    }

    private void callReject(int activityStep, int taskStep, String comment) {
        rejectCommand.execute(workflow.getWorkspaceId(), workflow.getId(), activityStep, taskStep, comment, this);
    }

    @Override
    public void onTaskStatusChange(TaskChangeEvent event) {
        if (event.getType() == Type.APPROVE) {
            callApprove(event.getActivity(), event.getStep(), event.getComment());
        } else {
            callReject(event.getActivity(), event.getStep(), event.getComment());

        }
    }

    
    private void createLayout() {       
        // very dirty workaround to get widget size !
        RootPanel.get().add(viewer);
        int width = viewer.getOffsetWidth();
        int height = viewer.getOffsetHeight();
        RootPanel.get().remove(viewer);
        if (width <= Window.getClientWidth() * RATIO && height <= Window.getClientHeight() * RATIO) {
            setWidget(viewer);    
        }
        else if (width <= Window.getClientWidth() * RATIO && height > Window.getClientHeight() * RATIO) {
            // width ok, but not height :
            scroll = new ScrollPanel(viewer);
            setWidget(scroll);
            scroll.setHeight((Window.getClientHeight() * RATIO) + "px");
            scroll.setWidth(width + "px");
        } else if (width > Window.getClientWidth() * RATIO && height <= Window.getClientHeight() * RATIO) {
            // height ok, but not width :
            scroll = new ScrollPanel(viewer);
            setWidget(scroll);
            scroll.setHeight((height + SCROLL_SIZE) + "px");
            scroll.setWidth((Window.getClientWidth() * RATIO) + "px");
        } else {
            // this viewer is realy too large ... or client is on EEEPC !!
            scroll = new ScrollPanel(viewer);
            setWidget(scroll);
            scroll.setHeight((Window.getClientHeight() * RATIO) + "px");
            scroll.setWidth((Window.getClientWidth() * RATIO) + "px");
        }
    }

    public void updateAfterAcceptOrReject(DocumentMasterDTO result) {
        viewer.hideAllPopups();
        viewer.removeFromParent();
        parentPanel.setLifeCycleState(result.getLifeCycleState());
        setWorkflow(result.getWorkflow());
    }

    public void setApproveAction(Action command) {
        approveCommand = command;
    }

    public void setRejectAction(Action command) {
        rejectCommand = command;
    }
}
