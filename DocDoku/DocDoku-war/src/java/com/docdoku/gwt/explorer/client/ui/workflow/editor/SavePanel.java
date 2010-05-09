/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.shared.ActivityModelDTO;
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
                List<ActivityModelDTO> activitiesList = m_editorn.getWorkflowModel().getData().getActivities();
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
