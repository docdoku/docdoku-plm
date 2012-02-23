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

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.server.rest.dto.InstanceAttributeTemplateDTO;
import com.google.gwt.user.client.ui.ListBox;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class AttributesTypesList extends ListBox {

    public AttributesTypesList() {
        ExplorerI18NConstants constants = ServiceLocator.getInstance().getExplorerI18NConstants();
        addItem(constants.textSearchAttribute());
        addItem(constants.numberSearchAttribute());
        addItem(constants.dateSearchAttribute());
        addItem(constants.booleanSearchAttribute());
        addItem(constants.urlSearchAttribute());
        setSelectedIndex(0);

    }

    public InstanceAttributeTemplateDTO.AttributeType getSelected() {
        InstanceAttributeTemplateDTO.AttributeType result = null;
        switch (getSelectedIndex()) {
            case 0:
                result = InstanceAttributeTemplateDTO.AttributeType.TEXT;
                break;
            case 1:
                result = InstanceAttributeTemplateDTO.AttributeType.NUMBER;
                break;
            case 2:
                result = InstanceAttributeTemplateDTO.AttributeType.DATE;
                break;
            case 3:
                result = InstanceAttributeTemplateDTO.AttributeType.BOOLEAN;
                break;
            case 4:
                result = InstanceAttributeTemplateDTO.AttributeType.URL;
                break;
        }

        return result;
    }
}

