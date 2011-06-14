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

package com.docdoku.gwt.explorer.client.ui.delegates;


import com.docdoku.gwt.explorer.client.ui.*;
import com.docdoku.gwt.explorer.client.data.MDocTableModel;
import com.docdoku.gwt.client.ui.widget.table.TableDelegate;
import com.docdoku.gwt.client.ui.widget.table.TableModelIndex;
import com.docdoku.gwt.client.ui.widget.table.TableWidget;
import com.google.gwt.user.client.ui.Image;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class VersionDelegate implements TableDelegate{

    private IconFactory iconFactory ;

    public VersionDelegate(IconFactory iconFactory) {
        this.iconFactory = iconFactory;
    }

    public void render(TableModelIndex index, int row, int column, TableWidget table, String[] tooltip) {
        Object value = table.getTableModel().getValueAt(index);
        MDocTableModel model = (MDocTableModel) table.getTableModel() ;
        if (value instanceof Boolean){
            Boolean castedValue = (Boolean) value ;
            if (castedValue){
                Image icon = iconFactory.createNewVersionIcon(model.getValueAt(index.getRow()).getWorkspaceId(), model.getValueAt(index.getRow()).getId(), model.getValueAt(index.getRow()).getVersion());
                table.setWidget(row, column, icon);
                table.getFlexCellFormatter().setWidth(row, column, "20px");
            }
        }
    }

}
