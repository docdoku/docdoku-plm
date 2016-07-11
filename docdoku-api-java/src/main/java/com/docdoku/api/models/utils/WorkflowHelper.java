/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
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

package com.docdoku.api.models.utils;

import com.docdoku.api.models.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class helps to manipulate workflow objects
 * @Author Morgan Guimard
 */
public class WorkflowHelper {

    public static ActivityDTO getCurrentActivity(WorkflowDTO workflow){

        List<ActivityDTO> activities = workflow.getActivities();
        int currentStep = getCurrentStep(workflow);
        if (currentStep < activities.size()) {
            return activities.get(currentStep);
        } else {
            return null;
        }

    }

    public static int getCurrentStep(WorkflowDTO workflow){
        List<ActivityDTO> activities = workflow.getActivities();
        int i = 0;
        for (ActivityDTO activity : activities) {
            if (activity.getComplete()) {
                i++;
            } else {
                break;
            }
        }
        return i;
    }


    public static List<TaskDTO> getRunningTasks(WorkflowDTO workflowDTO) {
        return getRunningTasks(getCurrentActivity(workflowDTO));
    }

    public static List<TaskDTO> getRunningTasks(ActivityDTO currentActivity) {
        List<TaskDTO> tasks = new ArrayList<>();
        for(TaskDTO task : currentActivity.getTasks()){
            if(TaskDTO.StatusEnum.IN_PROGRESS.equals(task.getStatus())){
                tasks.add(task);
            }
        }
        return tasks;
    }

    public static Set<RoleDTO> getRolesInvolved(WorkflowModelDTO workflowModel) {
        Set<RoleDTO> roles = new HashSet<>();
        for(ActivityModelDTO activityModelDTO:workflowModel.getActivityModels()){
            for(TaskModelDTO taskModelDTO : activityModelDTO.getTaskModels()){
                roles.add(taskModelDTO.getRole());
            }
        }
        return roles;
    }
}
