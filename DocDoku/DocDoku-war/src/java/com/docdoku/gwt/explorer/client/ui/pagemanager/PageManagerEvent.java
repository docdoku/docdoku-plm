/*
 * PageManagerEvent.java
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

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class PageManagerEvent extends GwtEvent<PageHandler>{

    private static Type<PageHandler> TYPE;

    public static void fire(HasPageHandlers source,int current, int numberOfPages) {
        if (TYPE != null) {
            PageManagerEvent event = new PageManagerEvent(current, numberOfPages);
            source.fireEvent(event);
        }
    }

    /**
     * Gets the type associated with this event.
     *
     * @return returns the handler type
     */
    public static Type<PageHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PageHandler>();
        }
        return TYPE;
    }

    private int currentPage ;
    private int numberOfPages ;

    public PageManagerEvent(int currentPage, int numberOfPages) {
        this.currentPage = currentPage;
        this.numberOfPages = numberOfPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    @Override
    public Type<PageHandler> getAssociatedType() {
        return (Type) TYPE;
    }

    @Override
    protected void dispatch(PageHandler handler) {
        handler.onPageChanged(this);
    }
    
}
