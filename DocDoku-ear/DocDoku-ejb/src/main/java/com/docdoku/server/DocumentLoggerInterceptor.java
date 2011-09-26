/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011 DocDoku SARL
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
package com.docdoku.server;

import com.docdoku.core.log.DocumentLog;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.document.Document;
import com.docdoku.server.dao.BinaryResourceDAO;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@LogDocument
@Interceptor
public class DocumentLoggerInterceptor {

    @PersistenceContext
    private EntityManager em;
    @Resource
    private SessionContext ejbCtx;
    private final static Logger LOGGER = Logger.getLogger(DocumentLoggerInterceptor.class.getName());
    private final static String EVENT = "DOWNLOAD";

    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        Object result = ctx.proceed();
        try {
            if (ctx.getParameters() != null && ctx.getParameters().length > 0 && ctx.getParameters()[0] instanceof String) {
                String fullName = (String) ctx.getParameters()[0];
                String userLogin = ejbCtx.getCallerPrincipal().toString();
                BinaryResourceDAO binDAO = new BinaryResourceDAO(em);
                BinaryResource file = binDAO.loadBinaryResource(fullName);
                Document document = binDAO.getDocumentOwner(file);
                if (document != null) {
                    DocumentLog log = new DocumentLog();
                    log.setUserLogin(userLogin);
                    log.setLogDate(new Date());
                    log.setDocumentWorkspaceId(document.getWorkspaceId());
                    log.setDocumentMasterDocumentId(document.getMasterDocumentId());
                    log.setDocumentMasterDocumentVersion(document.getMasterDocumentVersion());
                    log.setDocumentIteration(document.getIteration());
                    log.setEvent(EVENT);
                    log.setInfo(fullName);
                    em.persist(log);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return result;
    }
}
