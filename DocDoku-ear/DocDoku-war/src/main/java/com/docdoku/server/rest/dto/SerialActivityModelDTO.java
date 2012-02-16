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

package com.docdoku.server.rest.dto;

import com.docdoku.gwt.explorer.shared.TaskModelDTO;
import com.docdoku.server.rest.dto.ActivityModelDTO;
import java.io.Serializable;

public class SerialActivityModelDTO extends ActivityModelDTO implements Serializable {

    public SerialActivityModelDTO() {
        super();
    }

    public void moveUpTask(int index) {
        if (index > 0) {
            TaskModelDTO t = taskModels.get(index);
            this.taskModels.remove(index);
            this.taskModels.add(index - 1, t);
        }
    }

    public void moveDownTask(int index) {
        if (index < taskModels.size() - 1) {
            TaskModelDTO t = taskModels.get(index);
            this.taskModels.remove(index);
            this.taskModels.add(index + 1, t);
        }
    }
}
