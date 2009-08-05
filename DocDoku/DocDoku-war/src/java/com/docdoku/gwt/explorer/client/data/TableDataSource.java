package com.docdoku.gwt.explorer.client.data;

/**
 *
 * @author cooper
 */
public interface TableDataSource {

    /**
     * Returns the headers for the Table.
     * If there is no header to display, this method must return null.
     * @return header titles
     */
    public String[] getHeaderRow();

    /**
     * Returns the number of rows that contains data
     * @return
     */
    public int getRowCount();

    /**
     * Returns an array containing data for the specified row
     * @param row
     * @return
     */
    public String[] getRow(int row);

    /**
     * Returns the message that must be displayed when the model contains no data
     * @return
     */
    public String getEmptyCaseMessage() ;

    /**
     * Returns the tooltip(s) that must be displayed for the specified row
     * If the tooltip is more than one line long, each line is an array cell.
     * If there is no tooltip to display, this method must return null.
     * @param row
     * @return
     */
    public String[] getTooltipForRowColumn(int row, int column) ;
    
    
}
