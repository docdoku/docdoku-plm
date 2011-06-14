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
import com.google.gwt.user.client.ui.CheckBox;

public class BooleanAttributePanel extends AbstractAttributePanel {

	private CheckBox valueField ;
	
	public BooleanAttributePanel() {
		valueField = new CheckBox();
		add(valueField);
	}

    @Override
    public SearchQueryDTO.AbstractAttributeQueryDTO getAttribute() {
        if (getNameValue().isEmpty()){
            return null;
        }
        SearchQueryDTO.BooleanAttributeQueryDTO result = new SearchQueryDTO.BooleanAttributeQueryDTO(getNameValue(),valueField.getValue());
        return result;
    }
}
