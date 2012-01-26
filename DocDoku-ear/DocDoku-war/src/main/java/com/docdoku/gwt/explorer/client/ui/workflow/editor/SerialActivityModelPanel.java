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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.ActivityModelModel;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.SerialActivityModelEvent;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.SerialActivityModelHandler;
import com.docdoku.gwt.explorer.client.ui.workflow.editor.model.SerialActivityModelModel;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class SerialActivityModelPanel extends ActivityModelPanel implements SerialActivityModelHandler{

    private HandlerRegistration activityRegistration;


    public SerialActivityModelPanel(ScrollPanelUtil util) {
        super(util);
    }

    @Override
    public void setModel(ActivityModelModel model) {
        super.setModel(model);
        if (this.activityRegistration != null){
            activityRegistration.removeHandler();
        }
        if (model instanceof SerialActivityModelModel){
            this.model = model ;
            ((SerialActivityModelModel) model).addSerialActivityModelHandler(this);
            setupUi();
        }
        
    }

    private void setupUi(){
        for (int i = 0 ; i < model.getTasks().size() ; i++){
            TaskModelPanel panel = new TaskModelPanel(i!=0, i!=model.getTasks().size()-1, model.getTasks().size()!=1, util) ;
            taskPanels.add(panel);
            mainPanel.add(panel);
            panel.setModel(model.getTasks().get(i));
            panel.addListener(this);
        }
    }

    protected void addTask() {
        TaskModelPanel panel = new TaskModelPanel(true, false, true, util) ;
        mainPanel.add(panel);
        taskPanels.add(panel);
        updateTaskPanels();
        panel.setModel(model.getTasks().get(model.getTasks().size()-1)) ;
        panel.addListener(this);

    }

    public void onTaskMove(SerialActivityModelEvent event) {
        switch(event.getType()){
            case MOVE_UP :
                moveUpTaskPanel(event.getPosition()) ;
                break;
            case MOVE_DOWN:
                moveDownTaskPanel(event.getPosition());
                break;
        }
    }

    private void moveUpTaskPanel(int position) {
        TaskModelPanel panelTmp = taskPanels.remove(position);
        mainPanel.remove(panelTmp) ;
        mainPanel.insert(panelTmp, position-1);
        taskPanels.add(position-1, panelTmp);
        updateTaskPanels();
    }

    private void moveDownTaskPanel(int position) {
        TaskModelPanel panelTmp = taskPanels.remove(position);
        mainPanel.remove(panelTmp) ;
        mainPanel.insert(panelTmp, position+1);
        taskPanels.add(position+1, panelTmp);
        updateTaskPanels();
    }


    // it is not an optimal way to do it ...
    private void updateTaskPanels(){
        for (int i = 0; i < taskPanels.size();i++){
            taskPanels.get(i).setDeleteOptionEnabled(taskPanels.size()!=1);
            taskPanels.get(i).setLeftOptionEnabled(i!=0);
            taskPanels.get(i).setRightOptionEnabled(taskPanels.size()-1 != i);
        }
    }

    @Override
    protected  void removeTask(int position) {
        mainPanel.remove(position);
        TaskModelPanel p = taskPanels.remove(position);
        p.removeListener(this);
        updateTaskPanels();
    }

    

    @Override
    public void onMoveUpClicked(TaskModelPanelEvent ev) {
        ((SerialActivityModelModel)model).moveUpTask(taskPanels.indexOf(ev.getRealSource()));
    }

    @Override
    public void onMoveDownClicked(TaskModelPanelEvent ev) {
        ((SerialActivityModelModel)model).moveDownTask(taskPanels.indexOf(ev.getRealSource()));
    }
    

    
    

}
