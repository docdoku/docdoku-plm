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

package com.docdoku.gwt.explorer.client.ui.workflow.editor.model;

import com.docdoku.gwt.explorer.shared.ParallelActivityModelDTO;
import com.google.gwt.event.shared.HandlerRegistration;
/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ParallelActivityModelModel extends ActivityModelModel {


    public ParallelActivityModelModel(ParallelActivityModelDTO model, String workspaceId) {
        super(model, workspaceId);
    }
    
    public void setTasksToComplete (int amount){
        ((ParallelActivityModelDTO)model).setTasksToComplete(amount);
        ParallelActivityEvent.fire(this, amount);
    }

    public int getTaskToComplete (){
        return ((ParallelActivityModelDTO) model).getTasksToComplete();
    }

    public HandlerRegistration addParallelActivityModelHandler(ParallelActivityModelHandler handler){
        return addHandler(handler, ParallelActivityEvent.TYPE);
    }
}
