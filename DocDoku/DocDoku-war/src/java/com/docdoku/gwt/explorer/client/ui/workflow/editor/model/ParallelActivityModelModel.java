/*
 * ParallelActivityModelModel.java
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

package com.docdoku.gwt.explorer.client.ui.workflow.editor.model;

import com.docdoku.gwt.explorer.common.ParallelActivityModelDTO;
import com.docdoku.gwt.explorer.common.UserDTO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class ParallelActivityModelModel extends ActivityModelModel {

    private List<ParallelActivityModelListener> observers ;

    public ParallelActivityModelModel(ParallelActivityModelDTO model, String workspaceId) {
        super(model, workspaceId);
        observers = new ArrayList<ParallelActivityModelListener>() ;
    }
    
    public void setTasksToComplete (int amount){
        ((ParallelActivityModelDTO)model).setTasksToComplete(amount);
        
        ParallelActivityEvent event = new ParallelActivityEvent(this, amount);

        for (ParallelActivityModelListener listener : observers) {
            listener.onTaskRequiredChanged(event);
        }
    }

    public int getTaskToComplete (){
        return ((ParallelActivityModelDTO) model).getTasksToComplete();
    }

    @Override
    protected List<? extends ActivityModelListener> getListeners() {
        return observers ;
    }

    public void addListener (ParallelActivityModelListener l){
        observers.add(l);
    }

    public void removeListener( ParallelActivityModelListener l){
        observers.remove(l);
    }
}
