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

package com.docdoku.gwt.explorer.client;


import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

/**
 *
 * @author Florent Garin
 */
public class ExplorerActivityMapper implements ActivityMapper {
     //private ClientFactory clientfactory;
     
     
    
    @Override
    public Activity getActivity(Place place) {
        
      /*  
        
        if (place instanceof NewDocumentPlace)
        {return new NewDocumentActivity(clientfactory);}
        
        if (place instanceof NewFolderPlace)
        {return new NewFolderActivity(clientfactory);}
        
        if (place instanceof NewTemplatePlace)
        {return new NewModelActivity(clientfactory);}
        
         if (place instanceof NewWorkflowPlace)
        {return new NewWorkflowActivity(clientfactory);}
         
          if (place instanceof NewSearchPlace)
        {return new NewSearchActivity(clientfactory);}
       return null;
       */
        
        return null;
    }
    
}
