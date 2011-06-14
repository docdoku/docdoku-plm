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

package com.docdoku.gwt.explorer.client.ui.doc;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.docdoku.gwt.explorer.shared.MasterDocumentTemplateDTO;
import com.docdoku.gwt.explorer.shared.WorkflowModelDTO;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class CreateMDocMainPanel extends DataRoundedPanel{

    private TextBox m_parentTextBox;
    private ListBox m_templateListBox;
    private MaskLineEdit m_idTextBox;
    private TextBox m_titleTextBox;
    private ListBox m_workflowListBox;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public CreateMDocMainPanel(){
        createLayout();
    }

    private void createLayout() {
        inputPanel.setText(0,0,i18n.fieldLabelParentFolder());
        m_parentTextBox = new TextBox();
        inputPanel.setWidget(0,1,m_parentTextBox);

        inputPanel.setText(1,0,i18n.fieldLabelTemplate());
        m_templateListBox = new ListBox();
        m_templateListBox.setVisibleItemCount(1);
        inputPanel.setWidget(1,1,m_templateListBox);

        inputPanel.setText(2,0,i18n.fieldLabelIDMandatory());
        m_idTextBox = new MaskLineEdit();
        inputPanel.setWidget(2,1,m_idTextBox);

        inputPanel.setText(3,0,i18n.fieldLabelTitle());
        m_titleTextBox = new TextBox();
        inputPanel.setWidget(3,1,m_titleTextBox);

        inputPanel.setText(4,0,i18n.fieldLabelWorkflow());
        m_workflowListBox = new ListBox();
        m_workflowListBox.setVisibleItemCount(1);
        inputPanel.setWidget(4,1,m_workflowListBox);

    }

    public void setTemplates(MasterDocumentTemplateDTO[] templates){
        m_templateListBox.clear();
        m_templateListBox.addItem(ServiceLocator.getInstance().getExplorerI18NConstants().selectionNone());
        for(MasterDocumentTemplateDTO template:templates){
            String label = template.getId();
            String type = template.getDocumentType();

            if(type!=null && type.length()>0)
                label += " (" + type + ")";
            m_templateListBox.addItem(label, template.getId());

        }
    }

    public void setWorkflowModels(WorkflowModelDTO[] wks){
        m_workflowListBox.clear();
        m_workflowListBox.addItem(ServiceLocator.getInstance().getExplorerI18NConstants().selectionNone());
        for(WorkflowModelDTO wk:wks)
            m_workflowListBox.addItem(wk.getId());
    }


    public String getMDocId(){
        return m_idTextBox.getText();
    }

    public void setMDocId(String mdocId){
//        m_idTextBox.setText(mdocId);
        m_idTextBox.setGeneratedId(mdocId);
    }

    public void setMDocIdMask(String mask){
        m_idTextBox.setMask(mask);
    }

    public String getTemplateId(){
        int index = m_templateListBox.getSelectedIndex();
        return index<1?null:m_templateListBox.getValue(index);
    }

    public String getWorkflowModelId(){
        int index=m_workflowListBox.getSelectedIndex();
        return index<1?null:m_workflowListBox.getValue(index);
    }

    public String getMDocTitle(){
        return m_titleTextBox.getText();
    }

    public void setParentFolder(String parentFolder){
        m_parentTextBox.setText(parentFolder);
    }
    
    public String getParentFolderText(){
        return m_parentTextBox.getText();
    }

    public void clearInputs(){
        m_idTextBox.setText("");
        m_idTextBox.setMask("");
        m_titleTextBox.setText("");
    }

    public ListBox getTemplateListBox(){
        return m_templateListBox;
    }

    void setMDocIdEnabled(boolean b) {
        m_idTextBox.setText("");
        m_idTextBox.setMask("");
    }

    
}
