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

package com.docdoku.gwt.explorer.client.ui.pagemanager;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class PageManagerEvent extends GwtEvent<PageHandler> {

    private static Type<PageHandler> TYPE;

    public static void fire(HasPageHandlers source, int current, int numberOfPages, int start, int end, int total) {
        if (TYPE != null) {
            PageManagerEvent event = new PageManagerEvent(current, numberOfPages, start, end, total);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    @SuppressWarnings(value = "unchecked")
    public static Type<PageHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PageHandler>();
        }
        return TYPE;
    }
    private int currentPage;
    private int numberOfPages;
    private int start;
    private int end;
    private int total;

    public PageManagerEvent(int currentPage, int numberOfPages, int start, int end, int total) {
        this.currentPage = currentPage;
        this.numberOfPages = numberOfPages;
        this.start = start;
        this.end = end;
        this.total = total;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public Type<PageHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PageHandler handler) {
        handler.onPageChanged(this);
    }
}
