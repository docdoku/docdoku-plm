/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ExplorerConstants;
import com.docdoku.gwt.explorer.client.ui.doc.DocMainPanel;
import com.docdoku.gwt.explorer.client.ui.workflow.viewer.TaskChangeEvent.Type;
import com.docdoku.gwt.explorer.common.MasterDocumentDTO;
import com.docdoku.gwt.explorer.common.WorkflowDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.widgetideas.client.GlassPanel;

/**
 *
 * @author Emmanuel Nhan
 */
public class WorkflowGlassPanel extends GlassPanel implements ClickHandler, TaskListener {

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
        parentPanel = owner;
        viewer = null;
        scroll = null;


    }

    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
        this.visitorName = ExplorerConstants.getInstance().getUser().getName();
        viewer = new WorkflowViewer(workflow, visitorName, this);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        showViewer();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (scroll != null) {
            scroll.removeFromParent();
        }
        if (viewer.isAttached()) {
            viewer.removeFromParent();
        }

    }

    @Override
    public void onClick(ClickEvent event) {
        this.removeFromParent();
    }

    private void callApprove(int activityStep, int taskStep, String comment) {
        approveCommand.execute(workflow.getWorkspaceId(), workflow.getId(), activityStep, taskStep, comment, this);
    }

    private void callReject(int activityStep, int taskStep, String comment) {
        rejectCommand.execute(workflow.getWorkspaceId(), workflow.getId(), activityStep, taskStep, comment, this);
    }

    public void onTaskStatusChange(TaskChangeEvent event) {
        if (event.getType() == Type.APPROVE) {
            callApprove(event.getActivity(), event.getStep(), event.getComment());
        } else {
            callReject(event.getActivity(), event.getStep(), event.getComment());

        }
    }

    private void showViewer() {
        if (scroll != null) {
            scroll.removeFromParent();
        }
        RootPanel.get().add(viewer);
        // very dirty workaround to get widget size !
        int width = viewer.getOffsetWidth();
        int height = viewer.getOffsetHeight();
        viewer.setVisible(false);
        if (width <= Window.getClientWidth() * RATIO && height <= Window.getClientHeight() * RATIO) {
            viewer.setVisible(true);
            RootPanel.get().setWidgetPosition(viewer, Window.getClientWidth() / 2 - viewer.getOffsetWidth() / 2, Window.getClientHeight() / 2 - viewer.getOffsetHeight() / 2);

        } else if (width <= Window.getClientWidth() * RATIO && height > Window.getClientHeight() * RATIO) {
            // width ok, but not height :
            scroll = new ScrollPanel(viewer);
            scroll.setHeight((Window.getClientHeight() * RATIO) + "px");
            scroll.setWidth(width + "px");
            RootPanel.get().remove(viewer);
            viewer.setVisible(true);
            RootPanel.get().add(scroll);
            RootPanel.get().setWidgetPosition(scroll, Window.getClientWidth() / 2 - scroll.getOffsetWidth() / 2, Window.getClientHeight() / 2 - scroll.getOffsetHeight() / 2);
        } else if (width > Window.getClientWidth() * RATIO && height <= Window.getClientHeight() * RATIO) {
            // height ok, but not width :
            scroll = new ScrollPanel(viewer);
            scroll.setHeight((height + SCROLL_SIZE) + "px");
            scroll.setWidth((Window.getClientWidth() * RATIO) + "px");
            RootPanel.get().remove(viewer);
            viewer.setVisible(true);
            RootPanel.get().add(scroll);
            RootPanel.get().setWidgetPosition(scroll, Window.getClientWidth() / 2 - scroll.getOffsetWidth() / 2, Window.getClientHeight() / 2 - scroll.getOffsetHeight() / 2);
        } else {
            scroll = new ScrollPanel(viewer);
            // this viewer is realy too large ... or client is on EEEPC !!
            scroll.setHeight((Window.getClientHeight() * RATIO) + "px");
            scroll.setWidth((Window.getClientWidth() * RATIO) + "px");
            RootPanel.get().remove(viewer);
            viewer.setVisible(true);
            RootPanel.get().add(scroll);
            RootPanel.get().setWidgetPosition(scroll, Window.getClientWidth() / 2 - scroll.getOffsetWidth() / 2, Window.getClientHeight() / 2 - scroll.getOffsetHeight() / 2);
        }

    }

    public void updateAfterAcceptOrReject(MasterDocumentDTO result) {
        viewer.hideAllPopups();
        viewer.removeFromParent();
        parentPanel.setLifeCycleState(result.getLifeCycleState());
        setWorkflow(result.getWorkflow());
        showViewer();
    }

    public void setApproveAction(Action command) {
        approveCommand = command;
    }

    public void setRejectAction(Action command) {
        rejectCommand = command;
    }
}
