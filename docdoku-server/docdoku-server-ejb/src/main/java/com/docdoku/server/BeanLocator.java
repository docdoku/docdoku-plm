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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * Utility to lookup EJB implementing a given business interface from a JNDI
 * naming service.
 * 
 * @author Olivier Bourgeat
 *
 */
class BeanLocator {

    private static final Logger LOGGER = Logger.getLogger(BeanLocator.class.getName());
    
    @Resource(lookup="java:global")
    Context ctx;

    /**
     * Search for EJBs implementing a given business interface in the naming
     * namespace "java:global".
     * 
     * @param type
     *            Bean's Business Interface.
     * 
     * @return the list of EJBs implementing the given interface.
     * @throws NamingException
     */
    <T> List<T> search(Class<T> type) {
	List<T> result = new ArrayList<>();
	try {
	    result.addAll(search(type, ctx));
	} catch (NamingException e) {
	    LOGGER.log(Level.SEVERE,e.getMessage(),e);
	}
	return result;
    }

    /**
     * Search for EJBs implementing a given business interface within the given
     * Naming Context.
     * 
     * @param type
     *            EJB's Business Interface
     * @param ctx
     *            Current Naming Context
     * @return the list of EJBs implementing the given interface within the
     *         context.
     * @throws NamingException
     */
    @SuppressWarnings("unchecked") // Cause : Generic Type Erasure
    <T> List<T> search(Class<T> type, Context ctx) throws NamingException {
	NamingEnumeration<NameClassPair> ncps = ctx.list("");
	List<T> result = new ArrayList<T>();
	while (ncps.hasMoreElements()) {
	    NameClassPair ncp = ncps.next();
	    Object o = ctx.lookup(ncp.getName());
	    if (ncp.getName().contains("!" + type.getCanonicalName())) {
		// bean reference
		result.add((T) PortableRemoteObject.narrow(o, type));
	    } else if (Context.class.isAssignableFrom(o.getClass())) {
		// sub-context
		result.addAll(search(type, (Context) o));
	    }
	    // else ignore this object
	}
	return result;
    }
}