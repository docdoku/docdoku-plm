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

package com.docdoku.cli.commands.documents;

import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.services.DocumentsApi;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentSearchCommand extends BaseCommandLine {

    @Option(name = "-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage = "workspace on which operations occur")
    protected String workspace;

    @Option(name = "-s", aliases = "--search", required = true, metaVar = "<search>", usage = "search string")
    protected String searchValue;

    @Override
    public void execImpl() throws Exception {
        DocumentsApi documentsApi = new DocumentsApi(client);
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace, searchValue, null, null, null, null, null, null, null, null, null, null, null, null, null);
        output.printDocumentRevisions(documentRevisions);
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentSearchCommandDescription",user);
    }
}
