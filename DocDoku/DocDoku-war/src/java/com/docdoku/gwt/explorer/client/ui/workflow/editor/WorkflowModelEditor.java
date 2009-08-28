/*
 * WorkflowModelEditor.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.client.ui.widget.input.EditableLabel;
import com.docdoku.gwt.explorer.client.ui.workflow.EndPoint;
import com.docdoku.gwt.explorer.client.ui.workflow.StartPoint;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.SerialActivityModelModel;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.WorkflowModelEvent;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.WorkflowModelListener;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.WorkflowModelModel;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WorkflowModelEditor provides workflow edition
 * To store internal data, it uses a WorkflowModelModel by default
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class WorkflowModelEditor extends Composite implements ActivityLinkListener,WorkflowModelListener, ActivityModelPanelListener, ChangeHandler{

    // panels (general)
    private VerticalPanel mainPanel;
    private ScrollPanel scrollPanel ;
    private FlexTable canvas ;

    // panels (component logic)
    private List<ActivityLink> links ;
    private List<ActivityModelPanel> activities ;
    private List<EditableLabel> states ;
    private EditableLabel finalState ;

    private SavePanel savePanel ;
    
    
    // utils :
    private ScrollPanelUtil util ;

    // data :
    private WorkflowModelModel model ;

    public WorkflowModelEditor(final Map<String,Action> cmds) {
        mainPanel = new VerticalPanel() ;
        initWidget(mainPanel);

        scrollPanel = new ScrollPanel();
        mainPanel.add(scrollPanel);
        util = new ScrollPanelUtil();
        util.setScrollPanel(scrollPanel);

        savePanel = new SavePanel(this, cmds);
        mainPanel.add(savePanel);

        // lists init :
        links = new ArrayList<ActivityLink>() ;
        states= new ArrayList<EditableLabel>();
        activities = new ArrayList<ActivityModelPanel>();

        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    }

    public void setWorkflowModel( WorkflowModelModel model ){
        savePanel.setWorkflowModelName(model.getData().getId());
        this.model = model;
        model.addListener(this) ;
        clean();
        setupUi();
        loadWorkflowModel();
    }

    private void clean(){
        canvas = new FlexTable() ;
        canvas.setStyleName("canvas");
        scrollPanel.setWidget(canvas);

        links.clear();
        states.clear();
        activities.clear();
    }

    private void loadWorkflowModel(){

        for (int i = 0 ; i < model.getActivities().size() ; i++){
            addActivityPanel(i);
        }

        finalState.setText(model.getFinalStateName());
        
    }

    private void setupUi(){
        // create start point, end point, one link, and one state
        StartPoint sp = new StartPoint() ;
        EndPoint ep = new EndPoint() ;
        ActivityLink link = new ActivityLink() ;
        link.addListener(this);

        canvas.setWidget(0, 0, sp);
        canvas.setWidget(0, 1, link);
        canvas.setWidget(0, 2, ep);

        StateWidget state = new StateWidget() ;
        finalState = state.getStateEdit() ;
        finalState.addChangeHandler(this) ;
        canvas.setWidget(1, 2, state);

        links.add(link);

    }

    public void onSerialClicked(ActivityLinkEvent ev) {

        int index = links.indexOf(ev.getSource()) ;
        if (index != -1){
            model.addSerialActivity(index);
        }
    }

    public void onParallelClicked(ActivityLinkEvent ev) {
        int index = links.indexOf(ev.getSource());
        if (index != -1){
            model.addParallelActivity(index);
        }
    }

    
    private void addActivityPanel(int position){
        canvas.insertCell(0, position*2+2);
        ActivityLink tmpLink = new ActivityLink() ;
        links.add(position+1, tmpLink);
        tmpLink.addListener(this);
        canvas.setWidget(0, position*2+2, tmpLink);

        canvas.insertCell(0, position*2+2);
        ActivityModelPanel panel;
        if (model.getActivities().get(position) instanceof SerialActivityModelModel){
            panel = new SerialActivityModelPanel(util);
        }else{
            panel = new ParallelActivityModelPanel(util) ;
        }
        canvas.setWidget(0, position*2+2, panel);
        activities.add(position,panel) ;

        canvas.insertCell(1, position*2+2);
        canvas.insertCell(1, position*2+2);
        StateWidget state = new StateWidget() ;
        state.setText(model.getActivities().get(position).getLifeCycleState()) ;
        states.add(position, state.getStateEdit());
        canvas.setWidget(1, position*2+2, state);
        state.addChangeHandler(this) ;

        // set content
        panel.setModel(model.getActivities().get(position));

        // listener
        panel.addListener(this);
    }

    private void deleteActivityPanel(int position){

        states.remove(position);
        links.remove(position+1);
        canvas.removeCells(0, position*2+2, 2);
        canvas.removeCells(1, position*2+2, 2);
    }
    

    // for issues with scroll panel :
    public void setWidth (int size){
        scrollPanel.setWidth(size+"px");
    }

    public void onWorkflowModelChanged(WorkflowModelEvent event) {
        switch(event.getType()){
            case ACTIVITY_ADD:
                addActivityPanel(event.getPosition());
                break;
            case ACTIVITY_DELETE :
                deleteActivityPanel(event.getPosition());
                break ;
        }
    }

    public void onAddTaskClicked(ActivityModelPanelEvent event) {
        // we don't care ;)
    }

    public void onDeleteActivityClicked(ActivityModelPanelEvent event) {
        model.removeActivity(activities.indexOf(event.getRealSource()));
    }

    public WorkflowModelModel getWorkflowModel() {
        return model ;
    }

    public void onChange(ChangeEvent event) {
        if (event.getSource() == finalState){
            model.setFinalStateName(finalState.getText());
        }else{
            int index  = states.indexOf(event.getSource());
            model.setStateName(index, states.get(index).getText());
        }
    }


    

}
