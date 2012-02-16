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

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.server.rest.dto.ActivityModelDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import java.util.List;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;


/**
 *
 * @author manu
 */
public class SavePanel extends FlexTable implements KeyUpHandler{

    private Button saveButton;
    private WorkflowModelMainPanel m_mainPanel;
    private Label m_backAction;
    private WorkflowModelEditor m_editorn;

    public SavePanel(WorkflowModelEditor editor, final Map<String,Action> cmds){
        m_editorn = editor;
        ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

        this.saveButton = new Button(ServiceLocator.getInstance().getExplorerI18NConstants().saveButton());
        this.saveButton.setTitle(ServiceLocator.getInstance().getExplorerI18NConstants().saveTooltip());
        m_mainPanel = new WorkflowModelMainPanel();
        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);
        m_backAction=new Label(i18n.btnBack());
        m_backAction.setStyleName("normalLinkAction");
        m_backAction.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                cmds.get("BackCommand").execute();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                List<ActivityModelDTO> activitiesList = m_editorn.getWorkflowModel().getData().getActivityModels();
                ActivityModelDTO[] activities = activitiesList.toArray(new ActivityModelDTO[activitiesList.size()]);
                cmds.get("SaveWorkflowModelCommand").execute(m_mainPanel.getWorkflowModelID(), m_editorn.getWorkflowModel().getFinalStateName(),  activities);
            }
        });
        m_mainPanel.getWorkflowModelIDTextBox().addKeyUpHandler(this) ;
        buttonsPanel.add(m_backAction);
        buttonsPanel.add(saveButton);
        setWidget(0,0, m_mainPanel);
        setWidget(1,0,buttonsPanel);
    }

    public void setWorkflowModelName(String name){
        m_mainPanel.setWorkflowModelID(name);
    }
    
    @Override
    public void onKeyUp(KeyUpEvent event) {
       if (m_mainPanel.getWorkflowModelIDTextBox().getText().isEmpty()){
            saveButton.setEnabled(false);
        }else{
            saveButton.setEnabled(true);
        }
    }



}
