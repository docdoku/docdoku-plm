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
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Map;

public class ACLDAO {

    private final EntityManager em;

    public ACLDAO(EntityManager pEM) {
        em = pEM;
    }

    public void createACL(ACL acl) {
        //Hack to prevent a bug inside the JPA implementation (Eclipse Link)
        Map<UserGroup,ACLUserGroupEntry> groupEntries = acl.getGroupEntries();
        Map<User,ACLUserEntry> userEntries = acl.getUserEntries();
        acl.setGroupEntries(null);
        acl.setUserEntries(null);
        em.persist(acl);
        em.flush();
        acl.setGroupEntries(groupEntries);
        acl.setUserEntries(userEntries);
    }

    public void removeACLEntries(ACL acl){
        em.createNamedQuery("ACL.removeUserEntries").setParameter("aclId",acl.getId()).executeUpdate();
        em.createNamedQuery("ACL.removeUserGroupEntries").setParameter("aclId",acl.getId()).executeUpdate();
        em.flush();
    }

    public void removeAclUserEntries(User pUser) {
        Query query = em.createQuery("DELETE FROM ACLUserEntry a WHERE a.principal = :user");
        query.setParameter("user", pUser).executeUpdate();

    }
}
