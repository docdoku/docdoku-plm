/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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

package com.docdoku.gwt.explorer.client.ui.template;

import com.docdoku.gwt.explorer.client.actions.Action;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.FilesPanel;
import com.docdoku.gwt.explorer.shared.InstanceAttributeTemplateDTO;
import com.docdoku.gwt.explorer.shared.MasterDocumentTemplateDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import java.util.Map;
import org.cobogw.gwt.user.client.ui.Button;

/**
 *
 * @author Florent Garin
 */
public class MDocTemplatePanel extends FlexTable {

    private Button m_okBtn;
    private MDocTemplateMainPanel m_mainPanel;
    private FilesPanel m_filesPanel;
    private AttributesTemplatePanel m_attributesPanel;
    private Label m_backAction;
    private boolean m_creationMode=true;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public MDocTemplatePanel(final Map<String, Action> cmds) {
        FlexCellFormatter cellFormatter = getFlexCellFormatter();
        m_filesPanel = new FilesPanel();
        m_filesPanel.setEditionMode(true);
        m_attributesPanel = new AttributesTemplatePanel();
        m_filesPanel.injectDeleteAction(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("DeleteTemplateFileCommand").execute(m_filesPanel.getSelectedFiles());
            }
        });
        m_filesPanel.injectUploadAction(new ClickHandler() {

            public void onClick(ClickEvent event) {
                cmds.get("UploadTemplateFileCommand").execute();
            }
        });
        m_filesPanel.injectFormHandler(new FormPanel.SubmitCompleteHandler() {

            public void onSubmitComplete(SubmitCompleteEvent event) {
                cmds.get("UploadCompleteTemplateFileCommand").execute();
            }
        });
        m_mainPanel = new MDocTemplateMainPanel();

        HorizontalPanel buttonsPanel = new HorizontalPanel();
        buttonsPanel.setSpacing(5);

        m_backAction=new Label(i18n.btnBack());
        m_backAction.setStyleName("normalLinkAction");
        m_backAction.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                cmds.get("BackCommand").execute();
            }
        });
        m_okBtn = new Button(i18n.btnCreate());
        m_okBtn.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                InstanceAttributeTemplateDTO[] attrs = m_attributesPanel.getAttributes().toArray(new InstanceAttributeTemplateDTO[m_attributesPanel.getAttributes().size()]);
                String templateId=m_mainPanel.getMDocTemplateId();
                String documentType= m_mainPanel.getDocumentType();
                String mask=m_mainPanel.getMask();
                boolean idGenerated=m_mainPanel.isMDocTemplateIdGenerated();
                if(m_creationMode)
                    cmds.get("CreateMDocTemplateCommand").execute(templateId,documentType, mask, attrs, idGenerated);
                else
                    cmds.get("UpdateMDocTemplateCommand").execute(templateId,documentType, mask, attrs, idGenerated);
            }
        });
        buttonsPanel.add(m_backAction);
        buttonsPanel.add(m_okBtn);

        setWidget(0, 0, m_mainPanel);
        

        setWidget(1, 0, m_attributesPanel);

        setWidget(2, 0, buttonsPanel);
        cellFormatter.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        cellFormatter.setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
        
    }

    public void setCreationMode(boolean creationMode){
        m_mainPanel.setCreationMode(creationMode);
        m_creationMode=creationMode;
        FlexCellFormatter cellFormatter = getFlexCellFormatter();
        if(!m_creationMode){
            setWidget(0, 1, m_filesPanel);
            cellFormatter.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
            cellFormatter.setRowSpan(0, 1, 2);
            m_okBtn.setText(i18n.btnSave());
        }else{
            m_filesPanel.removeFromParent();
            m_okBtn.setText(i18n.btnCreate());
        }
    }
    public void clearInputs() {
        m_mainPanel.clearInputs();
        m_attributesPanel.clearInputs();
    }

    public FilesPanel getFilesPanel(){
        return m_filesPanel;
    }
    public void setFiles(Map<String,String> files){
        m_filesPanel.setFiles(files);
    }
    
    public void setTemplate(MasterDocumentTemplateDTO template) {
        m_mainPanel.setMDocTemplateAuthor(template.getAuthor().getName());
        m_mainPanel.setMDocTemplateGeneratedID(template.isIdGenerated());
        m_mainPanel.setMDocTemplateID(template.getId());
        m_mainPanel.setMDocTemplateMask(template.getMask());
        m_mainPanel.setMDocTemplateType(template.getDocumentType());
        m_attributesPanel.setAttributes(template.getAttributeTemplates());
        m_filesPanel.setFiles(template.getAttachedFiles());
    }

    
}
