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

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.log.DocumentLog;
import com.docdoku.core.security.UserGroupMapping;
import com.docdoku.core.services.IContextManagerLocal;
import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.server.dao.BinaryResourceDAO;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@LogDocument
@Interceptor
public class DocumentLoggerInterceptor implements Serializable{

    @Inject
    private EntityManagerProducer emf;

    @Inject
    private IDocumentManagerLocal documentManager;

    @Inject
    private IContextManagerLocal contextManager;

    private static final Logger LOGGER = Logger.getLogger(DocumentLoggerInterceptor.class.getName());
    private static final String EVENT = "DOWNLOAD";

    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {

        Object result = ctx.proceed();

        try {
            if (contextManager.isCallerInRole(UserGroupMapping.REGULAR_USER_ROLE_ID)
                    && ctx.getParameters() != null && ctx.getParameters().length > 0 && ctx.getParameters()[0] instanceof String) {
                String fullName = (String) ctx.getParameters()[0];
                String userLogin = contextManager.getCallerPrincipalLogin();

                BinaryResourceDAO binDAO = new BinaryResourceDAO(emf.create());
                BinaryResource file = binDAO.loadBinaryResource(fullName);
                DocumentIteration document = binDAO.getDocumentHolder(file);

                if (document != null) {
                    DocumentLog log = new DocumentLog();
                    log.setUserLogin(userLogin);
                    log.setLogDate(new Date());
                    log.setDocumentWorkspaceId(document.getWorkspaceId());
                    log.setDocumentId(document.getId());
                    log.setDocumentVersion(document.getVersion());
                    log.setDocumentIteration(document.getIteration());
                    log.setEvent(EVENT);
                    log.setInfo(fullName);
                    documentManager.createDocumentLog(log);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        return result;
    }
}
