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
import com.docdoku.api.services.FoldersApi;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentListCommand extends BaseCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-f", aliases = "--folder", usage="remote folder to list, default is workspace root folder")
    private String folder = null;

    @Option(name="-c", aliases = "--checkedOut", usage="list only checked out files")
    private boolean checkedOutDocsOnly = false;

    @Override
    public void execImpl() throws Exception {

        if(checkedOutDocsOnly){
            DocumentsApi documentsApi = new DocumentsApi(client);
            List<DocumentRevisionDTO> documentRevisions = documentsApi.getCheckedOutDocuments(workspace);
            output.printDocumentRevisions(documentRevisions);
        }else{
            FoldersApi foldersApi = new FoldersApi(client);
            String decodedPath = folder == null ? workspace : workspace+"/"+folder;
            List<DocumentRevisionDTO> documentRevisions = foldersApi.getDocumentsWithGivenFolderIdAndWorkspaceId(workspace,decodedPath);
            output.printDocumentRevisions(documentRevisions);
        }

    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentListCommandDescription",user);
    }
}
