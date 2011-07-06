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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class WorkflowModelMainPanel extends DataRoundedPanel{

    private TextBox m_idTextBox;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public WorkflowModelMainPanel(){
        inputPanel.setText(0,0,i18n.fieldLabelName());
        m_idTextBox = new TextBox();
        inputPanel.setWidget(0,1,m_idTextBox);
    }
    
    public void setWorkflowModelID(String name){
        m_idTextBox.setText(name);
    }
   
    public String getWorkflowModelID(){
        return m_idTextBox.getText();
    }
    public TextBox getWorkflowModelIDTextBox(){
        return m_idTextBox;
    }
    public void clearInputs(){
        m_idTextBox.setText("");
    }
}
