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
package com.docdoku.server;

import com.docdoku.core.services.ITaskManagerLocal;
import com.docdoku.core.services.IUserManagerLocal;
import com.docdoku.core.workflow.TaskKey;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@CheckActivity
@Interceptor
public class ActivityCheckerInterceptor {
    @Inject
    private IUserManagerLocal userManager;

    @Inject
    private ITaskManagerLocal taskManager;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {
        String workspaceId = (String) ctx.getParameters()[0];
        TaskKey taskKey = (TaskKey) ctx.getParameters()[1];
        taskManager.checkTask(workspaceId, taskKey);
        return ctx.proceed();
    }
}
