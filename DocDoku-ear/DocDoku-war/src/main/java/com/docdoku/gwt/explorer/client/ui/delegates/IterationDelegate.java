/*
 * IterationDelegate.java
 * 
 * Copyright (c) 2009 Docdoku. All rights reserved.
 * 
 * This file is part of Docdoku.
 * 
 * Docdoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Docdoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Docdoku.  If not, see <http://www.gnu.org/licenses/>.
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
public class IterationDelegate implements TableDelegate{

    private IconFactory iconFactory ;


    public IterationDelegate(IconFactory iconFactory) {
        this.iconFactory = iconFactory ;
    }

    public void render(TableModelIndex index, int row, int column, TableWidget table, String[] tooltip) {
        Object value = table.getTableModel().getValueAt(index);
        MDocTableModel model = (MDocTableModel) table.getTableModel();
        if (value instanceof Boolean) {
            Boolean castedValue = (Boolean) value;
            Image icon = null;
            if (castedValue) {
                icon = iconFactory.createIterationSubscriptionIcon(true, model.getValueAt(index.getRow()).getWorkspaceId(), model.getValueAt(index.getRow()).getId(), model.getValueAt(index.getRow()).getVersion());
            } else {
                icon = iconFactory.createIterationSubscriptionIcon(false, model.getValueAt(index.getRow()).getWorkspaceId(), model.getValueAt(index.getRow()).getId(), model.getValueAt(index.getRow()).getVersion());
            }
            table.setWidget(row, column, icon);
            table.getFlexCellFormatter().setWidth(row, column, "20px");
        }
    }

}
