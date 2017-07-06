/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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
 *
 * @Author Morgan Guimard
 */
public class WorkflowHelper {

    private WorkflowHelper() {
    }

    /**
     * Find the current activity in a workflow.
     *
     * @param workflow: the workflow to search in
     * @return the current activity of the given workflow
     */
    public static ActivityDTO getCurrentActivity(WorkflowDTO workflow) {

        List<ActivityDTO> activities = workflow.getActivities();
        int currentStep = getCurrentStep(workflow);
        if (currentStep < activities.size()) {
            return activities.get(currentStep);
        } else {
            return null;
        }

    }

    /**
     * Find the current activity step in a workflow
     *
     * @param workflow: the workflow to search in
     * @return the current activity step of the given workflow
     */
    public static int getCurrentStep(WorkflowDTO workflow) {
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

    /**
     * Get all running tasks in an activity
     *
     * @param activity: the activity to search in
     * @return the list of running tasks for the given activity
     */
    public static List<TaskDTO> getRunningTasks(ActivityDTO activity) {
        List<TaskDTO> tasks = new ArrayList<TaskDTO>();
        for (TaskDTO task : activity.getTasks()) {
            if (TaskDTO.StatusEnum.IN_PROGRESS.equals(task.getStatus())) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Get all roles involved in a workflow model
     *
     * @param workflowModel: the workflow model to search in
     * @return the list of distinct involved roles
     */
    public static Set<RoleDTO> getRolesInvolved(WorkflowModelDTO workflowModel) {
        Set<RoleDTO> roles = new HashSet<RoleDTO>();
        for (ActivityModelDTO activityModelDTO : workflowModel.getActivityModels()) {
            for (TaskModelDTO taskModelDTO : activityModelDTO.getTaskModels()) {
                roles.add(taskModelDTO.getRole());
            }
        }
        return roles;
    }
}
