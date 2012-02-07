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

package com.docdoku.gwt.explorer.client.ui.workflow.viewer;

import com.docdoku.server.rest.dto.TaskDTO;
import java.util.EventObject;

/**
 *
 * @author manu
 */
public class TaskChangeEvent extends EventObject {

    public enum Type{REJECT, APPROVE}

    private Type type ;
    private TaskDTO dtoSource ;
    private int activity ;
    private int step ;
    private String comment ;

    public TaskChangeEvent(Object source, Type type, TaskDTO dtoSource) {
        super(source);
        this.type = type ;
        this.dtoSource = dtoSource;
    }

    public TaskDTO getDtoSource() {
        return dtoSource;
    }

    public Type getType() {
        return type;
    }

    public int getActivity() {
        return activity;
    }

    public void setActivity(int activity) {
        this.activity = activity;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    
}
