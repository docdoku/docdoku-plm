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

package com.docdoku.gwt.explorer.client.ui.delegates;

import com.docdoku.gwt.explorer.client.data.DocMTableModel;
import com.docdoku.gwt.explorer.client.data.ServiceLocator;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.client.ui.widget.util.InteractiveHTML;
import com.docdoku.gwt.client.ui.widget.table.TableDelegate;
import com.docdoku.gwt.client.ui.widget.table.TableModelIndex;
import com.docdoku.gwt.client.ui.widget.table.TableWidget;
import com.docdoku.gwt.explorer.client.util.HTMLUtil;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class DocMIdDelegate implements TableDelegate{

    public void render(TableModelIndex index, int row, int column, TableWidget table, String[] tooltip) {
        ExplorerImageBundle imageBundle = ServiceLocator.getInstance().getExplorerImageBundle() ;
        String value = (String) table.getTableModel().getValueAt(index);
        DocMTableModel model = (DocMTableModel) table.getTableModel() ;
        String html =null;
        switch (model.getStatusAt(index.getRow())){
            case CHECKED_IN :
                html = HTMLUtil.imageItemHTML(imageBundle.documentRowIcon(), value);
                break ;
            case CHECKED_OUT_BY_OTHER :
                html = HTMLUtil.imageItemHTML(imageBundle.documentLockRowIcon(), value);
                break ;
            case CHECKED_OUT_BY_USER :
                html = HTMLUtil.imageItemHTML(imageBundle.documentEditRowIcon(), value);
                break ;
        }

        if (tooltip != null){
            InteractiveHTML item = new InteractiveHTML(html, tooltip, true);
            table.setWidget(row, column, item);
        }else{
            
            table.setHTML(row, column,html);
        }
    }

}
