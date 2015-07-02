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
import com.docdoku.core.exceptions.CreationException;
import com.docdoku.core.exceptions.RoleAlreadyExistsException;
import com.docdoku.core.exceptions.RoleNotFoundException;
import com.docdoku.core.workflow.Role;
import com.docdoku.core.workflow.RoleKey;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morgan Guimard
 */
public class RoleDAO {

    private EntityManager em;
    private Locale mLocale;

    private static final Logger LOGGER = Logger.getLogger(RoleDAO.class.getName());

    public RoleDAO(Locale pLocale, EntityManager pEM) {
        mLocale=pLocale;
        em=pEM;
    }

    public RoleDAO(EntityManager pEM) {
        mLocale=Locale.getDefault();
        em=pEM;
    }

    public Role loadRole(RoleKey roleKey) throws RoleNotFoundException {

        Role role = em.find(Role.class, roleKey);
        if (role == null) {
            throw new RoleNotFoundException(mLocale, roleKey);
        } else {
            return role;
        }

    }

    public List<Role> findRolesInWorkspace(String pWorkspaceId){
        return em.createNamedQuery("Role.findByWorkspace",Role.class).setParameter("workspaceId", pWorkspaceId).getResultList();
    }

    public void createRole(Role pRole) throws CreationException, RoleAlreadyExistsException {
        try{
            em.persist(pRole);
            em.flush();
        } catch (EntityExistsException pEEEx) {
            LOGGER.log(Level.FINEST,null,pEEEx);
            throw new RoleAlreadyExistsException(mLocale, pRole);
        } catch (PersistenceException pPEx) {
            LOGGER.log(Level.FINEST,null,pPEx);
            throw new CreationException(mLocale);
        }
    }

    public void deleteRole(Role pRole){
        em.remove(pRole);
        em.flush();
    }

    public boolean isRoleInUseInWorkflowModel(Role role) {
        return !em.createNamedQuery("Role.findRolesInUseByRoleName")
                 .setParameter("roleName", role.getName())
                 .setParameter("workspace", role.getWorkspace())
                 .getResultList().isEmpty();

    }

    public List<Role> findRolesInUseWorkspace(String pWorkspaceId) {
        return em.createNamedQuery("Role.findRolesInUse",Role.class).setParameter("workspaceId", pWorkspaceId).getResultList();
    }

    public void removeUserFromRoles(User pUser) {
        Query query = em.createQuery("UPDATE Role r SET r.defaultAssignee = NULL WHERE r.defaultAssignee = :user");
        query.setParameter("user", pUser).executeUpdate();
    }
}
