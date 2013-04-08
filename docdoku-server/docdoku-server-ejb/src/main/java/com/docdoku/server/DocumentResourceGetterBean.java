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
import resourcegetters.DocumentResourceGetter;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;


/**
 * Resource Getter
 */
@Stateless(name="DocumentResourceGetterBean")
public class DocumentResourceGetterBean implements IDocumentResourceGetterManagerLocal {

    @EJB
    private IDocumentManagerLocal documentService;

    @Inject
    @Any
    private Instance<DocumentResourceGetter> documentResourceGetters;

    @Resource(name = "vaultPath")
    private String vaultPath;

    @Override
    public File getDataFile(String resourceFullName, String subResourceName) throws Exception {
        DocumentResourceGetter selectedDocumentResourceGetter = null;
        File resourceFile = null;
        for (DocumentResourceGetter documentResourceGetter : documentResourceGetters) {
            if (documentResourceGetter.canGetResource(resourceFullName, subResourceName, vaultPath)) {
                selectedDocumentResourceGetter = documentResourceGetter;
                break;
            }
        }
        if (selectedDocumentResourceGetter != null) {
            resourceFile = selectedDocumentResourceGetter.getDataFile(resourceFullName, subResourceName, vaultPath);
        } else {
            resourceFile = documentService.getDataFile(resourceFullName);
        }
        return resourceFile;
    }

}
