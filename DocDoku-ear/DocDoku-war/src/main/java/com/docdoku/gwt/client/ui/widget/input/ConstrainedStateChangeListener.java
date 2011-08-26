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

package com.docdoku.gwt.client.ui.widget.input;

import java.util.EventListener;

/**
 *
 * @author Emmanuel Nhan <emmanuel.nhan@insa-lyon.fr>
 */
public interface ConstrainedStateChangeListener extends EventListener{

    /**
     * This method is called by a ConstrainedTextBox whenever its input state change.
     * ie :
     * <ul>
     * <li>When changing from Acceptable input to Unacceptable input</li>
     * <li>When changing from Unacceptable input to Acceptable input</li>
     * </ul>
     * @param event
     */
    public void onInputStateChange(ConstrainedStateChangeEvent event);

}
