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

package com.docdoku.server;

import com.docdoku.core.services.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
public class ServiceLocator {
    private static final String DATA_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/DataManagerBean!com.docdoku.core.services.IDataManagerLocal";
    private static final String PRODUCT_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/ProductManagerBean!com.docdoku.core.services.IProductManagerLocal";
    private static final String PRODUCT_INSTANCE_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/ProductInstanceManagerBean!com.docdoku.core.services.IProductInstanceManagerLocal";
    private static final String USER_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/UserManagerBean!com.docdoku.core.services.IUserManagerLocal";
    private static final String SHARE_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/ShareManagerBean!com.docdoku.core.services.IShareManagerLocal";
    private static final String PART_WORKFLOW_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/PartWorkflowManagerBean!com.docdoku.core.services.IPartWorkflowManagerLocal";
    private static final String DOCUMENT_WORKFLOW_MANAGER = "java:global/docdoku-server-ear/docdoku-server-ejb/DocumentWorkflowManagerBean!com.docdoku.core.services.IDocumentWorkflowManagerLocal";

    private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class.getName());

    private Context context;

    @PostConstruct
    private void init(){
        try{
            context = new InitialContext();
        }
        catch (NamingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @InternalService
    @Produces
    public IProductManagerLocal findProductManager() throws NamingException {
        return (IProductManagerLocal) context.lookup(PRODUCT_MANAGER);
    }

    @InternalService
    @Produces
    public IDataManagerLocal findDataManager() throws NamingException {
        return (IDataManagerLocal) context.lookup(DATA_MANAGER);
    }

    @InternalService
    @Produces
    public IProductInstanceManagerLocal findProductInstanceManager() throws NamingException {
        return (IProductInstanceManagerLocal) context.lookup(PRODUCT_INSTANCE_MANAGER);
    }

    @InternalService
    @Produces
    public IUserManagerLocal findUserManager() throws NamingException {
        return (IUserManagerLocal) context.lookup(USER_MANAGER);
    }

    @InternalService
    @Produces
    public IShareManagerLocal findShareManager() throws NamingException {
        return (IShareManagerLocal) context.lookup(SHARE_MANAGER);
    }

    @InternalService
    @Produces
    public IPartWorkflowManagerLocal findPartWorkflowManager() throws NamingException {
        return (IPartWorkflowManagerLocal) context.lookup(PART_WORKFLOW_MANAGER);
    }

    @InternalService
    @Produces
    public IDocumentWorkflowManagerLocal findDocumentWorkflowManager() throws NamingException {
        return (IDocumentWorkflowManagerLocal) context.lookup(DOCUMENT_WORKFLOW_MANAGER);
    }


}
