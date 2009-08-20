/*
 * PageManager.java
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

package com.docdoku.gwt.explorer.client.ui.pagemanager;

import com.docdoku.gwt.explorer.client.ui.widget.table.TableModel;
import com.docdoku.gwt.explorer.common.ExplorerServiceResponse;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Page count starts at 0 !
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class PageManager implements HasPageHandlers{

    private final static int DEFAULT_PAGE_SIZE = 4 ;

    private PageManagerBackend currentBackend ;
    private TableModel currentModel ;
    private int currentPage ;
    private int numberOfPages ;
    private int pageSize ;
    private HandlerManager handlerManager ;

    public PageManager() {
        handlerManager = new HandlerManager(this);
        pageSize = DEFAULT_PAGE_SIZE ;        
    }

    public void setPageManagerBackend(PageManagerBackend backend){
        currentBackend = backend ;
        currentBackend.setFrontend(this) ;
        currentBackend.fetchFirstPage();
    }

    // called only by backend (package visibility)
    void dataReady(ExplorerServiceResponse response){
        currentModel = currentBackend.getTableModel() ;
        numberOfPages = response.getTotalSize() / pageSize + 1;
        currentPage = response.getChunckOffset() / pageSize ;
        PageManagerEvent.fire(this, currentPage, numberOfPages);
    }

    public void fireEvent(GwtEvent<?> event) {
        handlerManager.fireEvent(event);
    }

    public HandlerRegistration addPageHandler(PageHandler handler) {
        return handlerManager.addHandler(PageManagerEvent.getType(), handler);
    }

    public PageManagerBackend getCurrentBackend() {
        return currentBackend;
    }

    public TableModel getCurrentModel() {
        return currentModel;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize > 0){
        this.pageSize = pageSize;
        }
    }

    public void fetchNext(){
        currentBackend.fetchNextPage();
    }

    public void fetchPrevious(){
        currentBackend.fetchPreviousPage();
    }

    public void fetchFirst(){
        currentBackend.fetchFirstPage();
    }

    public void fetchLast(){
        currentBackend.fetchLastPage();
    }

    int getOffsetForPageNumber(int pageNumber){
        return pageSize * pageNumber;
    }

}
