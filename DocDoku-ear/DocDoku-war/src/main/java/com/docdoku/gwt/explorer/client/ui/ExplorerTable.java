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

package com.docdoku.gwt.explorer.client.ui;

import com.docdoku.gwt.explorer.client.data.MDocTableModel;
import com.docdoku.gwt.client.ui.widget.table.TableClickHandler;
import com.docdoku.gwt.client.ui.widget.table.TableModel;
import com.docdoku.gwt.client.ui.widget.table.TableProfile;
import com.docdoku.gwt.client.ui.widget.table.TableWidget;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ExplorerTable extends Composite{

    private VerticalPanel mainPanel ;

    private TableWidget table ;

    private ExplorerMenuBar menuBarTop;
    private ExplorerMenuBar menuBarBottom ;
    private ExplorerDocumentMenuBar docMenuBarTop ;
    private ExplorerDocumentMenuBar docMenuBarBottom ;

    public ExplorerTable(ExplorerMenuBar menuBarTop, ExplorerMenuBar menuBarBottom, ExplorerDocumentMenuBar docMenuBarTop, ExplorerDocumentMenuBar docMenuBarBottom, DocDragController dndController) {
        this.menuBarTop = menuBarTop;
        this.menuBarBottom = menuBarBottom;
        this.docMenuBarTop = docMenuBarTop;
        this.docMenuBarBottom = docMenuBarBottom;

        mainPanel = new VerticalPanel();
        initWidget(mainPanel);

        table = new TableWidget(dndController);
        table.setWidth("100%");
    }

    

    public void setModel(TableModel model, TableProfile profile){
        mainPanel.clear();
        docMenuBarBottom.removeStyleName("myMenuBarSearch");
        docMenuBarTop.removeStyleName("myMenuBarSearch");
        if (model instanceof MDocTableModel){
            mainPanel.add(docMenuBarTop);
            mainPanel.add(table);
            mainPanel.add(docMenuBarBottom);
        }else{
            mainPanel.add(menuBarTop);
            mainPanel.add(table);
            mainPanel.add(menuBarBottom);
        }
        table.setModel(model, profile);
    }

    public void setModelSearch(TableModel model, TableProfile profile){
        mainPanel.clear();
        docMenuBarTop.addStyleName("myMenuBarSearch");
        docMenuBarBottom.addStyleName("myMenuBarSearch");
        if (model instanceof MDocTableModel){
            mainPanel.add(docMenuBarTop);
            mainPanel.add(table);
            mainPanel.add(docMenuBarBottom);
        }else{
            mainPanel.add(menuBarTop);
            mainPanel.add(table);
            mainPanel.add(menuBarBottom);
        }
        table.setModel(model, profile);
    }

    public HandlerRegistration addTableClickHandler(TableClickHandler handler) {
        return table.addTableClickHandler(handler);
    }

    public TableModel getTableModel() {
        return table.getTableModel();
    }

    public TableWidget getInnerTable(){
        return table ;
    }

    public void setSelectionForRows(List<Integer> rows, boolean selected) {
        table.setSelectionForRows(rows, selected);
    }

    public List<Integer> getSelectedRows() {
        return table.getSelectedRows();
    }

    public void unselectAllRows() {
        table.unselectAllRows();
    }

    public void selectRows(List<Integer> rows) {
        table.selectRows(rows);
    }

    public void selectAllRows() {
        table.selectAllRows();
    }

    
    

}
