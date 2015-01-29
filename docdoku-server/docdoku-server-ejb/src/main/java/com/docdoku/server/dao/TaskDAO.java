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

package com.docdoku.server.dao;

import com.docdoku.core.common.User;
import com.docdoku.core.exceptions.TaskNotFoundException;
import com.docdoku.core.workflow.Task;
import com.docdoku.core.workflow.TaskKey;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;

public class TaskDAO {
    
    private EntityManager em;
    private Locale mLocale;
    
    public TaskDAO(Locale pLocale, EntityManager pEM) {
        em = pEM;
        mLocale =pLocale;
    }
    
    public TaskDAO(EntityManager pEM) {
        em=pEM;
        mLocale=Locale.getDefault();
    }
    
    public Task loadTask(TaskKey pTaskKey) throws TaskNotFoundException {
        Task task = em.find(Task.class,pTaskKey);
        if (task == null) {
            throw new TaskNotFoundException(mLocale, pTaskKey);
        } else {
            return task;
        }
    }
    
    public Task[] findTasks(User pUser){
        Task[] tasks;
        Query query = em.createQuery("SELECT DISTINCT t FROM Task t WHERE t.worker = :user");
        query.setParameter("user",pUser);
        List listTasks = query.getResultList();
        tasks = new Task[listTasks.size()];
        for(int i=0;i<listTasks.size();i++) {
            tasks[i] = (Task) listTasks.get(i);
        }
        
        return tasks;
    }
    
}