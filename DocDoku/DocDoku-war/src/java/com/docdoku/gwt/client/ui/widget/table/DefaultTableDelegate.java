/*
 * DefaultTableDelegate.java
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

package com.docdoku.gwt.client.ui.widget.table;

import com.docdoku.gwt.explorer.client.ui.widget.InteractiveHTML;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.Date;

/**
 * Default renderer for a table cell
 * It displays dates in a short date format, strings and uses toString to display other objects
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class DefaultTableDelegate implements TableDelegate{


    public void render(TableModelIndex index, int row, int column, TableWidget table, String[] tooltip) {

        Object value = table.getTableModel().getValueAt(index);
        String stringValue ;
        if (value instanceof Date){
            stringValue = DateTimeFormat.getShortDateFormat().format((Date)value);
        }else if( value instanceof String){
            stringValue = (String) value ;
        }else{
            stringValue = value.toString() ;
        }

        if (tooltip != null){
            table.setWidget(row, column, new InteractiveHTML(stringValue, tooltip));
        }else{
            table.setHTML(row, column, stringValue);
        }
    }

}
