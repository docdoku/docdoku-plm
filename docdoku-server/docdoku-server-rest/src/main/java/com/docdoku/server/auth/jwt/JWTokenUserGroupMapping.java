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

package com.docdoku.server.auth.jwt;

import com.docdoku.core.security.UserGroupMapping;
import org.jose4j.jwt.JwtClaims;

/**
 * This JWTokenUserGroupMapping class wraps tokens decoded data
 *
 * @author Morgan Guimard
 */
public class JWTokenUserGroupMapping {
    private JwtClaims claims;
    private UserGroupMapping userGroupMapping;

    public JWTokenUserGroupMapping(JwtClaims claims, UserGroupMapping userGroupMapping) {
        this.claims = claims;
        this.userGroupMapping = userGroupMapping;
    }

    public JwtClaims getClaims() {
        return claims;
    }

    public UserGroupMapping getUserGroupMapping() {
        return userGroupMapping;
    }
}
