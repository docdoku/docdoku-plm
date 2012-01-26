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

package com.docdoku.gwt.explorer.client.ui.template;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent Garin
 */
public class DocMTemplateMainPanel extends DataRoundedPanel{

    private Label m_authorLabel;
    private TextBox m_idTextBox;
    private TextBox m_typeTextBox;
    private TextBox m_maskTextBox;
    private CheckBox m_generated;
    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public DocMTemplateMainPanel(){
        createLayout();
    }

    public void setCreationMode(boolean creationMode){
        m_idTextBox.setEnabled(creationMode);
        if(creationMode){
            inputPanel.setText(0,0,"");
            m_authorLabel.setText("");
        }else{
            inputPanel.setText(0,0,i18n.fieldLabelAuthor());
        }
    }
    private void createLayout() {

        inputPanel.setText(0,0,i18n.fieldLabelAuthor());
        m_authorLabel = new Label();
        inputPanel.setWidget(0,1,m_authorLabel);

        inputPanel.setText(1,0,i18n.fieldLabelIDMandatory());
        m_idTextBox = new TextBox();
        inputPanel.setWidget(1,1,m_idTextBox);

        inputPanel.setText(2,0,i18n.fieldLabelDocumentType());
        m_typeTextBox = new TextBox();
        inputPanel.setWidget(2,1,m_typeTextBox);

        inputPanel.setText(3,0,i18n.fieldLabelMask());
        m_maskTextBox=new TextBox();
        inputPanel.setWidget(3,1,m_maskTextBox);

        m_generated=new CheckBox(i18n.fieldLabelGeneratedID());
        inputPanel.setWidget(4,1,m_generated);

    }



    public String getDocMTemplateId(){
        return m_idTextBox.getText();
    }

    public String getMask(){
        return m_maskTextBox.getText();
    }

    public String getDocumentType(){
        return m_typeTextBox.getText();
    }

    public boolean isDocMTemplateIdGenerated(){
        return m_generated.getValue();
    }

    public void clearInputs(){
        m_authorLabel.setText("");
        m_idTextBox.setText("");
        m_maskTextBox.setText("");
        m_typeTextBox.setText("");
        m_generated.setValue(false);
    }

    public void setDocMTemplateAuthor(String author){
        m_authorLabel.setText(author);
    }
    public void setDocMTemplateId(String id){
        m_idTextBox.setText(id);
    }
    public void setDocMTemplateMask(String mask){
        m_maskTextBox.setText(mask);
    }
    public void setDocMTemplateType(String type){
        m_typeTextBox.setText(type);
    }
    public void setDocMTemplateGeneratedId(boolean generatedID){
        m_generated.setValue(generatedID);
    }
}
