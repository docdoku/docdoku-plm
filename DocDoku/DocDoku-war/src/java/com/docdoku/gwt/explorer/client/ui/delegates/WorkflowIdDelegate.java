/*
 * WorkflowIdDelegate.java
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

import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.client.ui.widget.table.TableDelegate;
import com.docdoku.gwt.explorer.client.ui.widget.table.TableModelIndex;
import com.docdoku.gwt.explorer.client.ui.widget.table.TableWidget;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class WorkflowIdDelegate implements TableDelegate{

    public void render(TableModelIndex index, int row, int column, TableWidget table, String[] tooltip) {
        ExplorerImageBundle imageBundle = ServiceLocator.getInstance().getExplorerImageBundle() ;
        String value = (String) table.getTableModel().getValueAt(index);
        String html = HTMLUtil.imageItemHTML(imageBundle.workflowRowIcon(), value) ;
        table.setHTML(row, column, html);
    }

}
