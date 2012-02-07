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
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.explorer.client.ui.workflow.viewer.WorkflowGlassPanel;
import com.docdoku.server.rest.dto.WorkflowDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Florent Garin
 */
public class DocMainPanel extends DataRoundedPanel {

    private Label m_idLabel;
    private Label m_authorLabel;
    private Label m_creationDateLabel;
    private Label m_checkOutUserLabel;
    private Label m_checkOutDateLabel;
    private Label m_titleLabel;
    private Label m_tagsLabel;
    private Label m_lifeCycleStateLabel;
    private Label m_typeLabel;
    private TextBox m_revisionNoteTextBox;
    private WorkflowGlassPanel m_workflowPanel;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    private final static int HEIGHT=320;
    
    public DocMainPanel(final Map<String, Action> cmds) {
        createLayout();
        m_workflowPanel.setApproveAction(cmds.get("ApproveCommand")) ;
        m_workflowPanel.setRejectAction(cmds.get("RejectCommand"));
    }

    
    
    private void createLayout() {
        setHeight(HEIGHT);
        inputPanel.setText(0, 0, i18n.fieldLabelID());
        m_idLabel = new Label();
        inputPanel.setWidget(0, 1, m_idLabel);

        inputPanel.setText(1, 0, i18n.fieldLabelAuthor());
        m_authorLabel = new Label();
        inputPanel.setWidget(1, 1, m_authorLabel);

        inputPanel.setText(2, 0, i18n.fieldLabelCreationDate());
        m_creationDateLabel = new Label();
        inputPanel.setWidget(2, 1, m_creationDateLabel);

        inputPanel.setText(3, 0, i18n.fieldLabelType());
        m_typeLabel = new Label();
        inputPanel.setWidget(3, 1, m_typeLabel);

        inputPanel.setText(4, 0, i18n.fieldLabelTitle());
        m_titleLabel = new Label();
        inputPanel.setWidget(4, 1, m_titleLabel);

        inputPanel.setText(5, 0, i18n.fieldLabelCheckOutUser());
        m_checkOutUserLabel = new Label();
        inputPanel.setWidget(5, 1, m_checkOutUserLabel);

        inputPanel.setText(6, 0, i18n.fieldLabelModificationDate());
        m_checkOutDateLabel = new Label();
        inputPanel.setWidget(6, 1, m_checkOutDateLabel);

        inputPanel.setText(7, 0, i18n.fieldLabelLifeCycleState());
        m_lifeCycleStateLabel = new Label();
        inputPanel.setWidget(7, 1, m_lifeCycleStateLabel);
        m_lifeCycleStateLabel.setStyleName("normalLinkAction");

        inputPanel.setText(8, 0, i18n.fieldLabelTags());
        m_tagsLabel = new Label();
        inputPanel.setWidget(8, 1, m_tagsLabel);

        inputPanel.setText(9, 0, i18n.fieldLabelRevisionNote());
        m_revisionNoteTextBox = new TextBox();
        inputPanel.setWidget(9, 1, m_revisionNoteTextBox);
        
        m_workflowPanel = new WorkflowGlassPanel(this);
    }

    public void setEditionMode(boolean editionMode){
        m_revisionNoteTextBox.setEnabled(editionMode);
    }

    public void setDocMAuthor(String pName) {
        m_authorLabel.setText(pName);
    }
    public void setCheckOutUser(String user) {
        m_checkOutUserLabel.setText(user);
    }

    public void setModificationDate(Date pModificationDate) {
        String date="";
        if(pModificationDate!=null)
            date=DateTimeFormat.getShortDateFormat().format(pModificationDate);
        
        m_checkOutDateLabel.setText(date);
    }
    public void setCreationDate(Date creationDate) {
        m_creationDateLabel.setText(DateTimeFormat.getShortDateFormat().format(creationDate));
    }

    public void setDocID(String pID) {
        m_idLabel.setText(pID);
    }


    public void setPermaLink(String permaLink){
        inputPanel.setHTML(0, 2, permaLink);
    }


    public void setDocMType(String type) {
        m_typeLabel.setText(type);
    }
    public void setDocMTitle(String title) {
        m_titleLabel.setText(title);
    }
    public void setLifeCycleState(String lifeCycleState) {
        m_lifeCycleStateLabel.setText(lifeCycleState);
        m_lifeCycleStateLabel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_workflowPanel.center();
            }
        });

    }

   public void setWorkflow(WorkflowDTO wk){
       m_workflowPanel.setWorkflow(wk);
   }


    public void setTags(String tags) {
        m_tagsLabel.setText(tags);
    }
    public String getRevisionNote() {
        return m_revisionNoteTextBox.getText();
    }

    public void setRevisionNote(String revisionNote) {
        m_revisionNoteTextBox.setText(revisionNote);
    }


    public void clearInputs() {
        m_authorLabel.setText("");
        m_checkOutDateLabel.setText("");
        m_checkOutUserLabel.setText("");
        m_idLabel.setText("");
        m_lifeCycleStateLabel.setText("");
        m_tagsLabel.setText("");
        m_titleLabel.setText("");
        m_typeLabel.setText("");
        m_revisionNoteTextBox.setText("");
        inputPanel.setHTML(0, 2, "");
    }
}
