/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License  
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.rest.util;

import com.docdoku.core.workflow.*;
import com.docdoku.server.rest.dto.ActivityDTO;
import com.docdoku.server.rest.dto.TaskDTO;
import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.DozerConverter;
import org.dozer.Mapper;

import java.util.ArrayList;
import java.util.List;


public class ActivityDozerConverter extends DozerConverter<Activity, ActivityDTO> {

    private Mapper mapper;

    public ActivityDozerConverter() {
        super(Activity.class, ActivityDTO.class);
        mapper = DozerBeanMapperSingletonWrapper.getInstance();
    }

    @Override
    public ActivityDTO convertTo(Activity activity, ActivityDTO activityDTO) {
        List<TaskDTO> tasksDTO = new ArrayList<TaskDTO>();

        for(int i=0; i<activity.getTasks().size(); i++){
            tasksDTO.add(mapper.map(activity.getTasks().get(i), TaskDTO.class));
        }

        ActivityDTO.Type type;
        Integer tasksToComplete = null;

        if (activity instanceof SerialActivity) {
            type = ActivityDTO.Type.SERIAL;
        } else if (activity instanceof ParallelActivity) {
            type = ActivityDTO.Type.PARALLEL;
            tasksToComplete = ((ParallelActivity) activity).getTasksToComplete();
        } else {
            throw new IllegalArgumentException("Activity type not supported");
        }

        return new ActivityDTO(activity.getStep(), tasksDTO, activity.getLifeCycleState(), type, tasksToComplete, activity.isComplete(), activity.isStopped());
    }

    @Override
    public Activity convertFrom(ActivityDTO activityDTO, Activity activity) {
        List<Task> tasks = new ArrayList<Task>();
        for(int i=0; i<activityDTO.getTasks().size(); i++){
            tasks.add(mapper.map(activityDTO.getTasks().get(i), Task.class));
        }

        switch (activityDTO.getType()){
            case SERIAL:{
                SerialActivity serialActivity = new SerialActivity();
                serialActivity.setStep(activityDTO.getStep());
                serialActivity.setTasks(tasks);
                serialActivity.setLifeCycleState(activityDTO.getLifeCycleState());
                return serialActivity;
            }
            case PARALLEL:{
                ParallelActivity parallelActivity = new ParallelActivity();
                parallelActivity.setStep(activityDTO.getStep());
                parallelActivity.setTasks(tasks);
                parallelActivity.setLifeCycleState(activityDTO.getLifeCycleState());
                parallelActivity.setTasksToComplete(activityDTO.getTasksToComplete());
                return parallelActivity;
            }
            default:{
                throw new IllegalArgumentException("ActivityDTO type not supported");
            }
        }
    }

}
