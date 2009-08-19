/*
 * TableProfile.java
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

package com.docdoku.gwt.explorer.client.ui.widget.table;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A table profile contains several informations to display a TableWidget
 * It handles all style related stuff and delegates for each column
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class TableProfile {

    static private final String DEFAULT_STYLE_PREFIX =  "myTable" ;

    private Map<Integer, TableDelegate> delegates ;
    private String stylePrefix ;
    private TableDelegate defaultDelegate ;
    private boolean selectionEnabled ;
    private boolean dndEnabled ;
    private Set<Integer> columnsNotToEmitClick ;

    public TableProfile() {
        delegates = new HashMap<Integer, TableDelegate>();
        columnsNotToEmitClick = new HashSet<Integer>();
        stylePrefix = DEFAULT_STYLE_PREFIX;
        defaultDelegate = new DefaultTableDelegate();
        selectionEnabled = true ;
        dndEnabled = false ;
    }

    public TableDelegate getDelegateForColumn(int column){
        TableDelegate delegate = delegates.get(column) ;
        if (delegate != null){
            return delegate ;
        }else{
            return defaultDelegate ;
        }
    }

    public String getStylePrefix() {
        return stylePrefix;
    }

    public void setStylePrefix(String stylePrefix) {
        this.stylePrefix = stylePrefix;
    }

    public void setDelegates(Map<Integer, TableDelegate> delegates) {
        this.delegates = delegates;
    }

    public void setDelegate (int column, TableDelegate delegate){
        delegates.put(column, delegate);
    }

    public TableDelegate getDefaultDelegate() {
        return defaultDelegate;
    }

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

    public Set<Integer> getColumnsNotToEmitClick() {
        return columnsNotToEmitClick;
    }

    public void addColumnNotToEmitClick(int column){
        columnsNotToEmitClick.add(column);
    }
}
