/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010 DocDoku SARL
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

package com.docdoku.server.dao;

import com.docdoku.core.TaskNotFoundException;
import com.docdoku.core.entities.Task;
import com.docdoku.core.entities.User;
import com.docdoku.core.entities.keys.TaskKey;
import java.util.List;
import java.util.Locale;


import javax.persistence.EntityManager;
import javax.persistence.Query;

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
        if (task == null)
            throw new TaskNotFoundException(mLocale, pTaskKey);
        else
            return task;
    }
    
    public Task[] findTasks(User pUser){
        Task[] tasks;
        Query query = em.createQuery("SELECT DISTINCT t FROM Task t WHERE t.worker = :user");
        query.setParameter("user",pUser);
        List listTasks = query.getResultList();
        tasks = new Task[listTasks.size()];
        for(int i=0;i<listTasks.size();i++)
            tasks[i]=(Task) listTasks.get(i);
        
        return tasks;
    }
    
}