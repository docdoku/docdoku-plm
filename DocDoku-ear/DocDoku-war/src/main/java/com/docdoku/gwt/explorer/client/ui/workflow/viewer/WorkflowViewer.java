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

import com.docdoku.gwt.explorer.client.ui.workflow.*;
import com.docdoku.gwt.explorer.shared.ActivityDTO;
import com.docdoku.gwt.explorer.shared.ParallelActivityDTO;
import com.docdoku.gwt.explorer.shared.SerialActivityDTO;
import com.docdoku.gwt.explorer.shared.WorkflowDTO;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import java.util.LinkedList;
import java.util.List;
import org.cobogw.gwt.user.client.ui.RoundedPanel;


public class WorkflowViewer extends Composite implements HasMouseOutHandlers, MouseOutHandler{

    private WorkflowDTO workflow;
    private FlexTable workflowViewer;
    private String visitorName;
    private List<ROActivityPanel> panels;
    
    public WorkflowViewer(WorkflowDTO workflow, String visitor, TaskListener l) {
        this.workflow = workflow;
        this.visitorName = visitor;
        workflowViewer = new FlexTable();
        panels = new LinkedList<ROActivityPanel>();
        addMouseOutHandler(this);
        initWidget(workflowViewer);
        setupUi(l);
    }

    void hideAllPopups() {
        for(ROActivityPanel p : panels){
            p.hideAllPopups();
        }
    }
    
    private void setupUi(TaskListener l) {
        populate( l);
        this.setStyleName("workflow-viewer");
    }

    private void populate(TaskListener listener) {
        // start point :
        workflowViewer.setWidget(0, 0, new StartPoint());
        workflowViewer.setWidget(0, 1, new HorizontalLink());
        for (int i = 0; i < workflow.getActivities().size(); i++) {
            // add an activity panel :
            ROActivityPanel activityPanel = createActivityPanel(workflow.getActivities().get(i), workflow.getCurrentStep() == i, i, listener);
            workflowViewer.setWidget(0, i * 2 + 2, activityPanel);
            panels.add(activityPanel);
            // add state name
            RoundedPanel rp = new RoundedPanel(RoundedPanel.ALL, 3) ;
            rp.setCornerStyleName("editableState-Corner");
            

            Label l = new Label(workflow.getActivities().get(i).getLifeCycleState());

            l.setStyleName("editableState-Element") ;

            rp.setWidget(l);
            l.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            workflowViewer.setWidget(1, i * 2 + 2, rp);
            workflowViewer.getCellFormatter().setHorizontalAlignment(1, i * 2 + 2, HasHorizontalAlignment.ALIGN_CENTER);
            
            // add link
            workflowViewer.setWidget(0, i * 2 + 3, new HorizontalLink());
        }
        workflowViewer.setWidget(0, workflow.getActivities().size() + 5, new EndPoint());
        RoundedPanel finalStatePanel = new RoundedPanel(RoundedPanel.ALL, 3);
        finalStatePanel.setCornerStyleName("editableState-Corner");
        Label finalLabel = new Label(workflow.getFinalLifeCycleState());
        finalLabel.setStyleName("editableState-Element") ;
        finalStatePanel.setWidget(finalLabel);
        workflowViewer.setWidget(1, workflow.getActivities().size() + 5, finalStatePanel);
    }

    private ROActivityPanel createActivityPanel(ActivityDTO abstractActivityDTO, boolean active, int step, TaskListener listener) {
        if (abstractActivityDTO instanceof SerialActivityDTO) {
            SerialActivityDTO dtos = (SerialActivityDTO) abstractActivityDTO;
            ROSerialActivityPanel panel = new ROSerialActivityPanel(dtos, visitorName, active, step, listener);
            return panel;
        } else {
            ParallelActivityDTO dtop = (ParallelActivityDTO) abstractActivityDTO;
            ROParallelActivityPanel panel = new ROParallelActivityPanel(dtop, visitorName, active, step, listener);
            return panel;
        }
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    public void onMouseOut(MouseOutEvent event) {
        int xMin = this.getAbsoluteLeft() ;
        int yMin = this.getAbsoluteTop();
        int xMax = xMin ;
        if (getOffsetWidth() > WorkflowGlassPanel.RATIO * Window.getClientWidth()){
            xMax += WorkflowGlassPanel.RATIO * Window.getClientWidth() ;
        } else {
            xMax += this.getOffsetWidth() ;
        }
        int yMax = yMin ;
        if(getOffsetHeight() > WorkflowGlassPanel.RATIO * Window.getClientHeight()){
            yMax += WorkflowGlassPanel.RATIO * Window.getClientHeight() ;
        }else{
            yMax +=this.getOffsetHeight();
        }

        int x = event.getClientX() ;
        int y = event.getClientY() ;
        if (x>xMax || x < xMin || y < yMin || y > yMax){
            for (ROActivityPanel rOActivityPanel : panels) {
                rOActivityPanel.hideAllPopups();
            }
        }
    }



}
