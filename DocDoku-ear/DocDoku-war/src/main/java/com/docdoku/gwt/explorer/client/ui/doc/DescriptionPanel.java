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
import com.docdoku.gwt.explorer.client.ui.widget.DataRoundedPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 *
 * @author Florent GARIN
 */
public class DescriptionPanel extends DataRoundedPanel{

    private TextArea m_descriptionTextArea;

    private final static int HEIGHT=200;
    
    public DescriptionPanel(){
        super(ServiceLocator.getInstance().getExplorerI18NConstants().tabDescription());
        createLayout();
    }

    private void createLayout() {
        setHeight(HEIGHT);
        m_descriptionTextArea = new TextArea();
        m_descriptionTextArea.setVisibleLines(8);
        m_descriptionTextArea.setCharacterWidth(25);
        
        inputPanel.setWidget(0,0,m_descriptionTextArea);
    }

    public void clearInputs(){
        m_descriptionTextArea.setText("");
    }

    public String getMDocDescription(){
        return m_descriptionTextArea.getText();
    }
}
