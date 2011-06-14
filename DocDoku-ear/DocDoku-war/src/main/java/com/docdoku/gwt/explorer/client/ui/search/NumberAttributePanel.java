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

package com.docdoku.gwt.explorer.client.ui.search;

import com.docdoku.gwt.explorer.shared.SearchQueryDTO;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

public class NumberAttributePanel extends AbstractAttributePanel implements ChangeHandler {
	
	private TextBox valueField;
	private String backup;
	
	public NumberAttributePanel() {
		valueField = new TextBox();
		add(valueField);
		backup = "";
		valueField.addChangeHandler(this);
		valueField.setTextAlignment(TextBox.ALIGN_RIGHT);
		valueField.setVisibleLength(4);
	}

	@Override
	public void onChange(ChangeEvent event) {
		
		if (!valueField.getText().isEmpty()){
			if (valueField.getText().matches("^[0-9]+(\\.|,)?[0-9]*")) {
				backup = valueField.getText() ;
			}else{
				valueField.setText(backup) ;
			}
		}
		
	}

    @Override
    public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null ;
        }
        SearchQueryDTO.NumberAttributeQueryDTO result = new SearchQueryDTO.NumberAttributeQueryDTO(getNameValue(),Float.parseFloat(valueField.getText()));
        return result;
    }

}
