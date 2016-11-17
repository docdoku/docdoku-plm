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

import com.docdoku.api.models.DocumentCreationDTO;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.services.FoldersApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.AccountsManager;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.LangHelper;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentCreationCommand extends BaseCommandLine {

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", required = true, usage = "the id of the document to save")
    private String id;

    @Option(metaVar = "<title>", name = "-N", aliases = "--title", usage = "the title of the document to save")
    private String title;

    @Option(metaVar = "<description>", name = "-d", aliases = "--description", usage = "the description of the document to save")
    private String description;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Argument(metaVar = "<file>", required = true, index=0, usage = "specify the file of the document to import")
    private File file;

    @Override
    public void execImpl() throws Exception {

        FoldersApi foldersApi = new FoldersApi(client);
        DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
        documentCreationDTO.setTitle(title);
        documentCreationDTO.setWorkspaceId(workspace);
        documentCreationDTO.setDescription(description);
        documentCreationDTO.setReference(id);

        foldersApi.createDocumentMasterInFolder(workspace, documentCreationDTO, workspace);
        DocumentRevisionDTO docRPK = new DocumentRevisionDTO();
        docRPK.setWorkspaceId(workspace);
        docRPK.setDocumentMasterId(id);
        docRPK.setVersion("A");

        DocumentIterationDTO docIPK = new DocumentIterationDTO();
        docIPK.setWorkspaceId(workspace);
        docIPK.setDocumentMasterId(id);
        docIPK.setVersion("A");
        docIPK.setIteration(1);

        FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));
        fh.uploadDocumentFile(getServerURL(), file, docIPK);
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentCreationCommandDescription",user);
    }
}
