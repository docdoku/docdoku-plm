/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.services.IDocumentResourceGetterManagerLocal;
import com.docdoku.server.resourcegetters.DocumentResourceGetter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;


/**
 * Resource Getter
 */
@Stateless(name="ResourceGetterBean")
public class ResourceGetterBean implements IDocumentResourceGetterManagerLocal {

    @EJB
    private IDocumentManagerLocal documentService;

    @Inject
    @Any
    private Instance<DocumentResourceGetter> resourceGetters;

    @Override
    public File getDataFile(String fullName) throws Exception {
        DocumentResourceGetter selectedResourceGetter = null;
        File resourceFile = null;
        for (DocumentResourceGetter resourceGetter : resourceGetters) {
            if (resourceGetter.canGetResource(fullName)) {
                selectedResourceGetter = resourceGetter;
                break;
            }
        }
        if (selectedResourceGetter != null) {
            resourceFile = selectedResourceGetter.getDataFile(fullName);
        } else {
            resourceFile = documentService.getDataFile(fullName);
        }
        return resourceFile;
    }

}
