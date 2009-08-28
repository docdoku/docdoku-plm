/*
 * ActivityLinkListener.java
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

package com.docdoku.gwt.explorer.client.ui.workflow.editor;

/**
 * The methods of this interface are called by an ActivityLink instance.
 * Each class interested in observing ActivityLinks must implement it.
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public interface ActivityLinkListener {

    /**
     * This method is called whanever a add serial activity image is clicked
     * @param ev
     */
    void onSerialClicked(ActivityLinkEvent ev) ;

    /**
     * This method is called whanever a add serial parallel image is clicked
     * @param ev
     */
    void onParallelClicked(ActivityLinkEvent ev) ;

}
