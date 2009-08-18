/*
 * Table.java
 */
package com.docdoku.gwt.explorer.client.ui;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.docdoku.gwt.explorer.client.data.TableDataSource;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author cooper
 */
public class Table extends FlexTable implements DragHandler {

    private static final int DEFAULT_ENTRIES_BY_PAGE = 10;
    private String headerStyle;
    private String selectedStyle;
    private TableDataSource source;
    private boolean dragNDropEnabled;
    private PickupDragController dndController;
    private int pagesEntryCount; // entries by page
    private int currentPage;
    private int numberOfPages;
    private List<TableListener> observers;

    /** Creates a new instance of Table */
    public Table(TableDataSource source, String stylePrefix, PickupDragController dndController) {
        super();
        observers = new LinkedList<TableListener>();
        this.dndController = dndController;
        this.setCellPadding(1);
        this.setCellSpacing(0);
        this.setWidth("100%");
        this.selectedStyle = stylePrefix + "-selected";
        this.headerStyle = stylePrefix + "-header";
        this.setSource(source, true);
        dndController.addDragHandler(this);
        ((DocDragController) dndController).setTable(this);
        dndController.setBehaviorDragProxy(true);
    }

    public TableDataSource getSource() {
        return source;
    }

    public void setSource(TableDataSource source, boolean dragNDrop) {
        this.source = source;
        dragNDropEnabled = dragNDrop;
        pagesEntryCount = DEFAULT_ENTRIES_BY_PAGE;
        currentPage = 0;

        for (int i = this.getRowCount(); i > 0; i--) {
            this.removeRow(0);
        }
        if (source == null) {
            return;
        } else {
            if (source.getRowCount() % pagesEntryCount != 0){
                numberOfPages = source.getRowCount() / pagesEntryCount + 1;
            }else{
                numberOfPages = source.getRowCount() /pagesEntryCount ;
            }
        }

        int row = 0;
        String[] headers = source.getHeaderRow();
        if (headers != null) {
            int col = 0;
            for (; col < headers.length; col++) {
                this.setHTML(row, col + 4, headers[col]);
            }
            this.setText(row, col + 4, "");
            this.setText(row, col + 5, "");
            this.getRowFormatter().addStyleName(row, headerStyle);
            row++;
        }
        if (source.getRowCount() != 0) {
            int beginning = currentPage * pagesEntryCount;
            int end = pagesEntryCount * (currentPage + 1);
            if (end > source.getRowCount()) {
                end = source.getRowCount();
            }

            for (int i = 0; i < source.getRowCount(); i++) {
                final CheckBox selection = new CheckBox();
                selection.setFormValue( i + "");
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
                if (dragNDropEnabled) {
                    Image im = new DraggableDocIcon(row);
                    dndController.makeDraggable(im, im);
                    this.setWidget(row, 0, im);
                    this.setWidget(row, 1, selection);

                } else {
                    this.setWidget(row, 0, selection);
                }


                String[] values = source.getRow(i);
                for (int col = 0; col < values.length; col++) {
                    if (source.getTooltipForRowColumn(i, col) != null) {
                        this.setWidget(row, col + 4, new InteractiveEntry(values[col], source.getTooltipForRowColumn(i, col)));
                    } else {
                        this.setHTML(row, col + 4, values[col]);
                    }
                }

                getFlexCellFormatter().setWidth(row, 0, "20px");
                getFlexCellFormatter().setWidth(row, 1, "20px");
                getFlexCellFormatter().setWidth(row, 2, "20px");
                if (dragNDropEnabled) {
                    getFlexCellFormatter().setWidth(row, 0, "10px");
                    getFlexCellFormatter().setWidth(row, 3, "20px");
                    getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
                    getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_MIDDLE);
                }

                // visibility:
                getRowFormatter().setVisible(row, i >= beginning && i < end);

                row++;
            }
        } else {
            Label l = new Label(source.getEmptyCaseMessage());
            this.setWidget(row, 0, l);

            getFlexCellFormatter().setColSpan(row, 0, getCellCount(0));
            getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER);
        }

        fireEvent();
    }

    public void setToIndicatorPanel(int row, int position, Widget widget) {

        int offset = 0;
        if (source.getHeaderRow() != null) {
            offset = 1;
        }
        int colOffset = 0;
        if (dragNDropEnabled) {
            colOffset = 1;
        }



        this.setWidget(row + offset, position + colOffset, widget);

    }

    public void setToCommandPanel(int row, int position, Widget widget) {

        int end = pagesEntryCount * (currentPage + 1);
        if (end > source.getRowCount()) {
            end = source.getRowCount();
        }


        int offset = 0;
        if (source.getHeaderRow() != null) {
            offset = 1;
        }
        int colOffset = 0;
        if (dragNDropEnabled) {
            colOffset = 1;
        }


        int col = source.getRow(row).length + 3 + position + colOffset;
        this.setWidget(row + offset, col, widget);
        getFlexCellFormatter().setWidth(row + offset, col, "20px");

    }

    public List<Integer> getSelectedRows() {
        List<Integer> rows = new ArrayList<Integer>();
        if (source.getRowCount() != 0) {
            int offset = 0;
            if (source.getHeaderRow() != null) {
                offset = 1;
            }

            for (int i = offset; i < getRowCount(); i++) {
                CheckBox selection = (CheckBox) getWidget(i, getCheckBoxColumnIndex());
                if (selection.getValue()) {
                    rows.add(i - offset);
                }
            }
        }
        return rows;
    }

    public void selectAllRows() {
        setSelectionForAllRows(true);
    }

    public void unselectAllRows() {
        setSelectionForAllRows(false);
    }

    /**
     * 
     * @param rows
     * @param selected
     */
    public void setSelectionForRows(List<Integer> rows, boolean selected) {
        for (Integer integer : rows) {
            int row = integer;
            if (source.getHeaderRow() != null) {
                row++;
            }
            CheckBox selection = (CheckBox) getWidget(row, getCheckBoxColumnIndex());
            selection.setValue(selected);

            if (selected) {
                getRowFormatter().addStyleName(row, selectedStyle);
            } else {
                getRowFormatter().removeStyleName(row, selectedStyle);
            }
        }
    }

    private void setSelectionForAllRows(boolean selected) {
        int offset = 0;
        if (source.getHeaderRow() != null) {
            offset = 1;
        }
        for (int i = offset; i < getRowCount(); i++) {
            if (getRowFormatter().isVisible(i)) {
                CheckBox selection = (CheckBox) getWidget(i, getCheckBoxColumnIndex());
                selection.setValue(selected);

                if (selected) {
                    getRowFormatter().addStyleName(i, selectedStyle);
                } else {
                    getRowFormatter().removeStyleName(i, selectedStyle);
                }
            }
        }
    }

    private int getCheckBoxColumnIndex() {
        if (dragNDropEnabled) {
            return 1;
        } else {
            return 0;
        }
    }

    public void onDragEnd(DragEndEvent event) {
    }

    public void onDragStart(DragStartEvent event) {
    }

    public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
    }

    public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {

        DraggableDocIcon sourceIcon = (DraggableDocIcon) event.getSource();
        CheckBox selection = (CheckBox) getWidget(sourceIcon.getRow(), getCheckBoxColumnIndex());
        if (!selection.getValue()) {
            unselectAllRows();
            selection.setValue(true);
            getRowFormatter().addStyleName(sourceIcon.getRow(), selectedStyle);
        }

    }

    public boolean isDragNDropEnabled() {
        return dragNDropEnabled;
    }

    public boolean isInCommandZone(int colIndex) {
        int colOffset = 0;
        if (dragNDropEnabled) {
            colOffset = 1;
        }

        if (source.getRowCount() != 0) {

            boolean res = colIndex >= source.getRow(0).length + 3 + colOffset;
            return res;


        } else {
            return false;
        }

    }

    private class TooltipPanel extends DecoratedPopupPanel {

        private HorizontalPanel panel;

        public TooltipPanel(String input[]) {
            panel = new HorizontalPanel();
            for (String str : input) {
                panel.add(new InlineLabel(str));
            }
            setWidget(panel);
        }
    }

    private class InteractiveEntry extends HTML implements MouseMoveHandler, MouseOutHandler, ClickHandler {

        private TooltipPanel tooltipPanel;

        public InteractiveEntry(String html, String tooltip[]) {
            super(html);
            this.tooltipPanel = new TooltipPanel(tooltip);
            addMouseMoveHandler(this);
            addMouseOutHandler(this);
            addClickHandler(this);

        }

        public void onMouseMove(MouseMoveEvent event) {
//            tooltipPanel.setPopupPosition(event.getClientX() + 10, event.getClientY() + 10);
            if (!tooltipPanel.isShowing()) {
//                tooltipPanel.show();
                tooltipPanel.showRelativeTo(this);
            }
        }

        public void onMouseOut(MouseOutEvent event) {
            tooltipPanel.hide();
        }

        public void onClick(ClickEvent event) {
            tooltipPanel.hide();
        }
    }

    public void setStylePrefix(String stylePrefix) {
        this.selectedStyle = stylePrefix + "-selected";
        this.headerStyle = stylePrefix + "-header";

        if (source != null && source.getHeaderRow() != null) {
            getRowFormatter().setStyleName(0, headerStyle);
        }


    }

    public void showFirstPage() {
        showPage(0);
    }

    public void showLastPage() {
        showPage(numberOfPages - 1);
    }

    public void showPreviousPage() {
        if (currentPage != 0) {
            showPage(currentPage - 1);
        }
    }

    public void showNextPage() {
        if (currentPage != numberOfPages - 1) {
            showPage(currentPage + 1);
        }
    }

    public void showPage(int pageNumber) {
        currentPage = pageNumber;

        int beginning = currentPage * pagesEntryCount;
        int end = pagesEntryCount * (currentPage + 1);
        if (end > source.getRowCount()) {
            end = source.getRowCount();
        }
        int offset = 0;
        if (source.getHeaderRow() != null) {
            offset = 1;
        }
        for (int i = 0; i < source.getRowCount(); i++) {

            getRowFormatter().setVisible(i + offset, i >= beginning && i < end);
        }
        // fire event :
        fireEvent();
    }

    public void addListener(TableListener l) {
        observers.add(l);
    }

    public void removeListener(TableListener l) {
        observers.remove(l);
    }

    private void fireEvent() {
        TableEvent event = new TableEvent(this);
        for (TableListener listener : observers) {
            listener.onPageChanged(event);
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public int getEntiesByPage() {
        return pagesEntryCount;
    }
}
