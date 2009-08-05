/*
 * SerialActivityModelModel.java
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

import com.docdoku.gwt.explorer.common.SerialActivityModelDTO;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Emmanuel Nhan {@literal <emmanuel.nhan@insa-lyon.fr>}
 */
public class SerialActivityModelModel extends ActivityModelModel{
    
    private List<SerialActivityModelListener> observers ;

    public SerialActivityModelModel(SerialActivityModelDTO model, String workspaceId) {
        super(model, workspaceId);
        observers = new ArrayList<SerialActivityModelListener>() ;
    }
    
    public void moveUpTask(int position){
        ((SerialActivityModelDTO)model).moveUpTask(position);
        taskModels.add(position-1, taskModels.remove(position));

        SerialActivityModelEvent event = new SerialActivityModelEvent(this, position, SerialActivityModelEvent.MoveType.MOVE_UP);
        for (SerialActivityModelListener listener : observers) {
            listener.onTaskMove(event);
        }
    }
    
    public void moveDownTask(int position){
        ((SerialActivityModelDTO) model).moveDownTask(position);
        taskModels.add(position+1, taskModels.remove(position));

        SerialActivityModelEvent event = new SerialActivityModelEvent(this, position, SerialActivityModelEvent.MoveType.MOVE_DOWN);
        for (SerialActivityModelListener listener : observers) {
            listener.onTaskMove(event);
        }
    }

    @Override
    protected List<? extends ActivityModelListener> getListeners() {
        return observers ;
    }

    public void addListener(SerialActivityModelListener l){
        observers.add(l);
    }

    public void removeListener(SerialActivityModelListener l){
        observers.remove(l);
    }
    
}
