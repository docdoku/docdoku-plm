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

package com.docdoku.gwt.explorer.client.ui.doc;


import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.shared.ACLDTO;
import com.docdoku.server.rest.dto.DocumentMasterTemplateDTO;
import com.docdoku.server.rest.dto.WorkflowModelDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;

/**
 *
 * @author Florent GARIN
 */
public class CreateDocMPanel extends FlexTable {

    private Button m_okBtn;
    private Label m_backAction;
    private DescriptionPanel m_descriptionPanel;
    //private SecurityPanel m_securityPanel;
    private CreateDocMMainPanel m_mainPanel;

    
    
    public CreateDocMPanel(final Map<String, Action> cmds) {
        ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();
        FlexCellFormatter cellFormatter = getFlexCellFormatter();
        m_descriptionPanel = new DescriptionPanel();
        //m_securityPanel= new SecurityPanel();
        m_mainPanel = new CreateDocMMainPanel();

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);

        m_backAction=new Label(i18n.btnBack());
        m_backAction.setStyleName("normalLinkAction");
        m_backAction.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                cmds.get("BackCommand").execute();
            }
        });
        m_okBtn = new Button(i18n.btnCreate());
        m_okBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                //ACLDTO acl=m_securityPanel.getACL();
                ACLDTO acl=null;
                cmds.get("CreateDocMCommand").execute(m_mainPanel.getParentFolderText(),m_mainPanel.getDocMTitle(), m_mainPanel.getDocMId(), m_mainPanel.getTemplateId(), m_mainPanel.getWorkflowModelId(), m_descriptionPanel.getDocMDescription(),acl);
            }
        });
        buttonsPanel.add(m_backAction);
        buttonsPanel.add(m_okBtn);
       
        setWidget(0,0, m_mainPanel);
        setWidget(0,1, m_descriptionPanel);
        //setWidget(0,2, m_securityPanel);
        setWidget(1,0,buttonsPanel);
        cellFormatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        //cellFormatter.setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
    }

    public void clearInputs() {
        m_mainPanel.clearInputs();
        m_descriptionPanel.clearInputs();
        //m_securityPanel.clearInputs();
    }

    public String getTemplateId(){
        return m_mainPanel.getTemplateId();
    }
    public ListBox getTemplateListBox(){
        return m_mainPanel.getTemplateListBox();
    }
    public void setParentFolder(String parentFolder){
        m_mainPanel.setParentFolder(parentFolder);
    }
    
    public void setTemplates(DocumentMasterTemplateDTO[] templates) {
        m_mainPanel.setTemplates(templates);
    }

    public void setDocMId(String docMId){
        m_mainPanel.setDocMId(docMId);
    }

    public void setDocMIdMask(String mask) {
        m_mainPanel.setDocMIdMask(mask);
    }

    public void setWorkflowModels(WorkflowModelDTO[] wks) {
        m_mainPanel.setWorkflowModels(wks);
    }

    /*
    public void setUserMemberships(UserDTO[] userMSs){
        m_securityPanel.setUserMemberships(userMSs);
    }

    public void setUserGroupMemberships(UserGroupDTO[] groupMSs){
        m_securityPanel.setUserGroupMemberships(groupMSs);
    }
    */
    
    public void setDocMIdEnabled(boolean b) {
        m_mainPanel.setDocMIdEnabled(b) ;
    }
}
