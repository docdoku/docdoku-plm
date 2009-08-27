/*
 * TableWidget.java
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

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TableWidget extends FlexTable implements DragHandler, HasTableClickHandlers, ClickHandler {

    private TableProfile profile;
    private boolean selectionEnabled;
    private boolean dragNDropEnabled;
    private PickupDragController dndController;
    private String headerStyle;
    private String selectedStyle;
    private TableModel data;

    public TableWidget(PickupDragController dndController) {
        this.setCellPadding(1);
        this.setCellSpacing(0);
        this.setWidth("100%");
        profile = new TableProfile();
        this.dndController = dndController;
        addClickHandler(this);
        dndController.addDragHandler(this);
    }

    public void setModel(TableModel data, TableProfile profile) {
        this.profile = profile;
        headerStyle = profile.getStylePrefix() + "-header";
        selectedStyle = profile.getStylePrefix() + "-selected";
        dragNDropEnabled = profile.isDndEnabled();
        selectionEnabled = profile.isSelectionEnabled();
        setModel(data);
    }

    public void setModel(TableModel data) {
        this.data = data;
        setupRows();

    }

    private void clearRows() {
        for (int i = this.getRowCount(); i > 0; i--) {
            this.removeRow(0);
        }
    }

    private void setupRows() {
        clearRows();

        // column offset :
        int columnOffset = 0;
        if (selectionEnabled) {
            columnOffset++;
        }
        if (dragNDropEnabled) {
            columnOffset++;
        }

        // row offset + header row
        int rowOffset = 0;
        if (data.getHeaderRow() != null) {
            String headers[] = data.getHeaderRow();
            for (int i = 0; i < headers.length; i++) {
                if (headers[i] != null) {
                    setHTML(0, i + columnOffset, headers[i]);
                } else {
                    setHTML(0, i + columnOffset, "");
                }
            }
            this.getRowFormatter().addStyleName(0, headerStyle);
            rowOffset++;
        }


        // data
        if (data.getRowCount() != 0) {
            for (int i = 0; i < data.getRowCount(); i++) {
                for (int j = 0; j < data.getColumnCount(); j++) {

                    // selection check box
                    final CheckBox selection = new CheckBox();
                    selection.setFormValue(i + "");
                    selection.addClickHandler(new ClickHandler() {

                        public void onClick(ClickEvent event) {
                            int row = Integer.parseInt(selection.getFormValue());
                            if (selection.getValue()) {
                                getRowFormatter().addStyleName(row + 1, selectedStyle);
                            } else {
                                getRowFormatter().removeStyleName(row + 1, selectedStyle);
                            }
                        }
                    });

                    // drag n drop icon (TODO : replace this by a delegate to get the draggable widget)
                    Image dndIcon = new DefaultTableDragIcon(i+rowOffset);
                    dndController.makeDraggable(dndIcon, dndIcon);

                    if (dragNDropEnabled) {
                        setWidget(i + rowOffset, 0, dndIcon);
                        getFlexCellFormatter().setWidth(i + rowOffset, 0, "10px");
                        getFlexCellFormatter().setAlignment(i+rowOffset, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
                        if (selectionEnabled) {
                            setWidget(i + rowOffset, 1, selection);
                            getFlexCellFormatter().setWidth(i + rowOffset, 1, "20px");
                        }

                    } else {
                        if (selectionEnabled) {
                            setWidget(i + rowOffset, 0, selection);
                            getFlexCellFormatter().setWidth(i + rowOffset, 0, "20px");
                        }
                    }

                    profile.getDelegateForColumn(j).render(new TableModelIndex(i, j), i + rowOffset, j + columnOffset, this, data.getTooltip(i, j));

                }
            }
        } else {
            Label l = new Label(data.getEmptyCaseMessage());
            this.setWidget(rowOffset, 0, l);

            getFlexCellFormatter().setColSpan(rowOffset, 0, getCellCount(0));
            getCellFormatter().setHorizontalAlignment(rowOffset, 0, HasHorizontalAlignment.ALIGN_CENTER);
        }
    }

    public TableModel getTableModel() {
        return data;
    }

    public void onDragEnd(DragEndEvent event) {
    }

    public void onDragStart(DragStartEvent event) {
    }

    public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
    }

    public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
        TableDragIcon sourceIcon = (TableDragIcon) event.getSource();
        CheckBox selection = (CheckBox) getWidget(sourceIcon.getRow(), getSelectionColumnIndex());
        if (!selection.getValue()) {
            unselectAllRows();
            selection.setValue(true);
            getRowFormatter().addStyleName(sourceIcon.getRow(), selectedStyle);
        }
    }

    public boolean isDragNDropEnabled() {
        return dragNDropEnabled;
    }

    public boolean isSelectionEnabled() {
        return selectionEnabled;
    }

    public void setDragNDropEnabled(boolean dragNDropEnabled) {
        this.dragNDropEnabled = dragNDropEnabled;
        setupRows();
    }

    public void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
        setupRows();
    }

    public TableProfile getProfile() {
        return profile;
    }

    public void setProfile(TableProfile profile) {
        setModel(data, profile);
    }

    public void selectAllRows() {
        if (selectionEnabled && data.getRowCount() != 0) {
            int offsetRow = 0;
            if (data.getHeaderRow() != null) {
                offsetRow++;
            }

            for (int i = offsetRow; i < getRowCount(); i++) {
                setSelectionStatus(i, true);
            }
        }
    }

    public void unselectAllRows() {
        if (selectionEnabled && data.getRowCount() != 0) {
            int offsetRow = 0;
            if (data.getHeaderRow() != null) {
                offsetRow++;
            }

            for (int i = offsetRow; i < getRowCount(); i++) {
                setSelectionStatus(i, false);
            }
        }
    }

    private int getSelectionColumnIndex() {
        if (selectionEnabled && dragNDropEnabled) {
            return 1;
        } else {
            return 0;
        }
    }

    private void setSelectionStatus(int row, boolean selected) {
        int checkBoxIndex = getSelectionColumnIndex();
        CheckBox checkBox = (CheckBox) getWidget(row, checkBoxIndex);
        if (checkBox.getValue() != selected) {
            checkBox.setValue(selected);
            if (selected) {
                getRowFormatter().addStyleName(row, selectedStyle);
            } else {
                getRowFormatter().removeStyleName(row, selectedStyle);
            }


        }
    }

    public List<Integer> getSelectedRows() {
        List<Integer> result = new LinkedList<Integer>();
        if (selectionEnabled) {
            int rowOffset = 0;
            if (data.getHeaderRow() != null) {
                rowOffset++;
            }

            for (int i = rowOffset; i < getRowCount(); i++) {
                if (((CheckBox) getWidget(i, getSelectionColumnIndex())).getValue()) {
                    result.add(i - rowOffset);
                }
            }
        }
        return result;
    }

    public void setSelectionForRows(List<Integer> rows, boolean selected) {
        int offsetRow = 0;
        if (data.getHeaderRow() != null) {
            offsetRow++;
        }
        for (Integer i : rows) {
            setSelectionStatus(i + offsetRow, selected);
        }
    }

    public void selectRows(List<Integer> rows) {
        if (selectionEnabled && data.getRowCount() != 0) {
            int offsetRow = 0;
            if (data.getHeaderRow() != null) {
                offsetRow++;
            }
            for (int i = offsetRow; i < getRowCount(); i++) {

                setSelectionStatus(i, rows.contains(i - offsetRow));
            }
        }

    }

    public HandlerRegistration addTableClickHandler(TableClickHandler handler) {
        return addHandler(handler, TableClickEvent.getType());
    }

    public void onClick(ClickEvent event) {
        if (event.getSource() == this) {
            Cell source = getCellForEvent(event);
            if ((source.getCellIndex()- getColumnOffset() >= 0) && !(profile.getColumnsNotToEmitClick().contains(source.getCellIndex()- getColumnOffset() ) || (source.getRowIndex() == 0 && data.getHeaderRow() != null))) {
                TableClickEvent.fire(this, new TableModelIndex(source.getRowIndex() - getRowOffset(), source.getCellIndex() - getColumnOffset()));
            }
        }
    }

    private int getRowOffset(){
        int res = 0 ;
        if (data != null && data.getHeaderRow() != null){
            res ++ ;
        }
        return res ;
    }

    private int getColumnOffset(){
        int res = 0 ;
        if (selectionEnabled){
            res ++ ;
        }
        if (dragNDropEnabled){
            res ++ ;
        }
        return res ;
    }
}
