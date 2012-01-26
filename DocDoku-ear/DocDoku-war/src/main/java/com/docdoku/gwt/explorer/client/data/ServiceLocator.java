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

package com.docdoku.gwt.explorer.client.data;

import com.docdoku.gwt.explorer.client.localization.ExplorerI18NConstants;
import com.docdoku.gwt.explorer.client.resources.icons.ExplorerImageBundle;
import com.docdoku.gwt.explorer.shared.ExplorerService;
import com.docdoku.gwt.explorer.shared.ExplorerServiceAsync;
import com.google.gwt.core.client.GWT;

/**
 *
 * @author Florent Garin
 */
public class ServiceLocator {


    private final ExplorerServiceAsync m_explorerService = (ExplorerServiceAsync) GWT.create(ExplorerService.class);
    private final ExplorerI18NConstants m_explorerConstants = (ExplorerI18NConstants) GWT.create(ExplorerI18NConstants.class);
    private final ExplorerImageBundle m_imageBundle = (ExplorerImageBundle) GWT.create(ExplorerImageBundle.class);
    
    private final static ServiceLocator s_instance = new ServiceLocator();

    private ServiceLocator() {
        
    }
    
    public static ServiceLocator getInstance() {
        return s_instance;
    }

    
    public ExplorerServiceAsync getExplorerService(){
        return m_explorerService;
    }
    
    public ExplorerI18NConstants getExplorerI18NConstants(){
        return m_explorerConstants;
    }
    
    public ExplorerImageBundle getExplorerImageBundle(){
        return m_imageBundle;
    }
}
