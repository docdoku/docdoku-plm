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

package com.docdoku.gwt.explorer.client.ui.folder;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 *
 * @author Florent GARIN
 */
public class CreateFolderMainPanel extends DataRoundedPanel{

    private TextBox m_parentTextBox;
    private TextBox m_folderTextBox;

    private final ExplorerI18NConstants i18n = ServiceLocator.getInstance().getExplorerI18NConstants();

    public CreateFolderMainPanel(){
        inputPanel.setText(0,0,i18n.fieldLabelParentFolder());
        m_parentTextBox = new TextBox();
        inputPanel.setWidget(0,1,m_parentTextBox);

        inputPanel.setText(1,0,i18n.fieldLabelName());
        m_folderTextBox = new TextBox();
        inputPanel.setWidget(1,1,m_folderTextBox);
    }
    
    public void setParentFolder(String parentFolder){
        m_parentTextBox.setText(parentFolder);
    }
    public String getParentFolderText(){
        return m_parentTextBox.getText();
    }
    public String getFolderText(){
        return m_folderTextBox.getText();
    }
    
    public void clearInputs(){
        m_folderTextBox.setText("");
    }
}
