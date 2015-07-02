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

package com.docdoku.server.rest.converters;

import com.docdoku.core.common.User;
import com.docdoku.core.common.UserGroup;
import com.docdoku.core.security.ACL;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.server.rest.dto.ACLDTO;
import org.dozer.DozerConverter;

import java.util.Map;


public class AclDozerConverter extends DozerConverter<ACL, ACLDTO> {


    public AclDozerConverter() {
        super(ACL.class, ACLDTO.class);
    }

    @Override
    public ACLDTO convertTo(ACL acl, ACLDTO aclDTO) {

        aclDTO = new ACLDTO();

        if(acl != null){

            for (Map.Entry<User,ACLUserEntry> entry : acl.getUserEntries().entrySet()) {
                ACLUserEntry aclEntry = entry.getValue();
                aclDTO.addUserEntry(aclEntry.getPrincipalLogin(),aclEntry.getPermission());
            }

            for (Map.Entry<UserGroup,ACLUserGroupEntry> entry : acl.getGroupEntries().entrySet()) {
                ACLUserGroupEntry aclEntry = entry.getValue();
                aclDTO.addGroupEntry(aclEntry.getPrincipalId(),aclEntry.getPermission());
            }
            return aclDTO;
        }

        return null;
    }

    @Override
    public ACL convertFrom(ACLDTO aclDTO, ACL acl) {
        return acl;
    }

}
