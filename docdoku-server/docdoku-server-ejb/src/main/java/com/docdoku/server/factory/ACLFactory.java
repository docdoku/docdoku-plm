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
package com.docdoku.server.factory;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.common.UserGroupKey;
import com.docdoku.core.common.UserKey;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLPermission;
import com.docdoku.server.dao.ACLDAO;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Asmae CHADID on 26/02/15.
 */
public class ACLFactory {
    private EntityManager em;

    public ACLFactory(EntityManager em) {
        this.em = em;
    }

    public ACL createACL(String pWorkspaceId, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) {
        ACL acl = new ACL();
        if (pUserEntries != null) {
            for (Map.Entry<String, String> entry : pUserEntries.entrySet()) {
                acl.addEntry(em.find(User.class, new UserKey(pWorkspaceId, entry.getKey())),
                        ACLPermission.valueOf(entry.getValue()));
            }
        }
        if (pGroupEntries != null) {
            for (Map.Entry<String, String> entry : pGroupEntries.entrySet()) {
                acl.addEntry(em.find(UserGroup.class, new UserGroupKey(pWorkspaceId, entry.getKey())),
                        ACLPermission.valueOf(entry.getValue()));
            }
        }
        new ACLDAO(em).createACL(acl);
        return acl;
    }

    public ACL updateACL(String workspaceId, ACL acl, Map<String, String> pUserEntries, Map<String, String> pGroupEntries) {

        if (acl != null) {
            new ACLDAO(em).removeACLEntries(acl);
            acl.setUserEntries(new HashMap<>());
            acl.setGroupEntries(new HashMap<>());
            for (Map.Entry<String, String> entry : pUserEntries.entrySet()) {
                acl.addEntry(em.getReference(User.class, new UserKey(workspaceId, entry.getKey())), ACLPermission.valueOf(entry.getValue()));
            }

            for (Map.Entry<String, String> entry : pGroupEntries.entrySet()) {
                acl.addEntry(em.getReference(UserGroup.class, new UserGroupKey(workspaceId, entry.getKey())), ACLPermission.valueOf(entry.getValue()));
            }
        }
        return acl;
    }
}
