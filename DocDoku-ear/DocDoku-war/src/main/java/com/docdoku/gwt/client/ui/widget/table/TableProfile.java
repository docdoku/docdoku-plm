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

package com.docdoku.gwt.client.ui.widget.table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A table profile contains several informations to display a TableWidget
 * It handles all style related stuff and delegates for each column
 * It is usefull to determines whenever a table element is activated via a click
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TableProfile {

    private final static String PRIMARY_STYLE =  "docdoku-TableWidget";

    private Map<Integer, TableDelegate> delegates ;
    private String stylePrefix;
    private TableDelegate defaultDelegate;
    private boolean selectionEnabled;
    private boolean dndEnabled;
    private Set<Integer> columnsNotToEmitClick;

    /**
     * Builds a default TableProfile
     * It uses a default delegate to render all cells, allow selection, and dnd (Drag'n drop) is not enabled
     */
    public TableProfile() {
        delegates = new HashMap<Integer, TableDelegate>();
        columnsNotToEmitClick = new HashSet<Integer>();
        stylePrefix = PRIMARY_STYLE;
        defaultDelegate = new DefaultTableDelegate();
        selectionEnabled = true ;
        dndEnabled = false ;
    }

    /**
     * Get the delegate used to render cells in column <code>column</code>
     * @param column
     * @return
     */
    public TableDelegate getDelegateForColumn(int column){
        TableDelegate delegate = delegates.get(column) ;
        if (delegate != null){
            return delegate ;
        }else{
            return defaultDelegate ;
        }
    }

    /**
     * Get the style prefix of the profile
     * @return
     */
    public String getStylePrefix() {
        return stylePrefix;
    }

    /**
     * Set the style prefix for this profile
     * @param stylePrefix
     */
    public void setStylePrefix(String stylePrefix) {
        this.stylePrefix = stylePrefix;
    }

    /**
     * replace all existing delegates for this profile with <code>delegates</code>
     * @param delegates
     */
    public void setDelegates(Map<Integer, TableDelegate> delegates) {
        this.delegates = delegates;
    }

    /**
     * Add to the delegates collection the delegate <code>delegate</code> associated with the column
     * if this column already has a delegate, it is replaced by the new one
     * @param column
     * @param delegate
     */
    public void setDelegate (int column, TableDelegate delegate){
        delegates.put(column, delegate);
    }

    /**
     * TableProfile returns a default delegate if a delegate was not explicitly specified for a column
     * This method returns this default delegate
     * @return
     */
    public TableDelegate getDefaultDelegate() {
        return defaultDelegate;
    }

    /**
     * Specify which default delegate to use. By default, it is a <code>DefaultTableDelegate</code>
     * @param defaultDelegate
     */
    public void setDefaultDelegate(TableDelegate defaultDelegate) {
        this.defaultDelegate = defaultDelegate;
    }

    public boolean isDndEnabled() {
        return dndEnabled;
    }

    public void setDndEnabled(boolean dndEnabled) {
        this.dndEnabled = dndEnabled;
    }

    public boolean isSelectionEnabled() {
        return selectionEnabled;
    }

    public void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
    }

    /**
     * get the column indexes for which activation by click does not mean anything
     * @return
     */
    public Set<Integer> getColumnsNotToEmitClick() {
        return columnsNotToEmitClick;
    }

    /**
     * add a column index which will not be process if a click append in it
     * @param column
     */
    public void addColumnNotToEmitClick(int column){
        columnsNotToEmitClick.add(column);
    }
}
