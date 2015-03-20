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


package com.docdoku.server.configuration.spec;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.DocumentConfigSpec;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;

/**
 * @author Morgan Guimard
 *
 */

public class LatestDocumentConfigSpec extends DocumentConfigSpec {

    private User user;
    public LatestDocumentConfigSpec() {
    }

    public LatestDocumentConfigSpec(User user) {
        this.user = user;
    }

    @Override
    public DocumentIteration filter(DocumentRevision documentRevision) {
        return documentRevision.getLastIteration();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
