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
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.services.DocumentApi;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentStatusCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the document to get a status ('A', 'B'...); if not specified the document identity (id and revision) corresponding to the file will be selected")
    private String revision;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the document to get a status; if not specified choose the document corresponding to the file")
    private String id;

    @Argument(metaVar = "[<file>]", index=0, usage = "specify the file of the document to get a status")
    private File file;

    @Option(name="-w", aliases = "--workspace", required = false, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    private long lastModified;

    @Override
    public void execImpl() throws Exception {
        try {

            if(id==null || revision==null || workspace==null){
                loadMetadata();
            }

            DocumentApi documentApi = new DocumentApi(client);

            DocumentRevisionDTO documentRevisionDTO = documentApi.getDocumentRevision(workspace,id,revision);
            output.printDocumentRevision(documentRevisionDTO,lastModified);

        } catch (ApiException e) {
            MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
            meta.deleteEntryInfo(file.getAbsolutePath());
            output.printException(e);
        }
    }

    private void loadMetadata() throws IOException {
        if(file==null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified1",user));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
        String filePath = file.getAbsolutePath();
        id = meta.getDocumentId(filePath);
        workspace = meta.getWorkspace(filePath);
        lastModified = meta.getLastModifiedDate(filePath);
        String strRevision = meta.getRevision(filePath);
        if(id==null || strRevision==null || workspace == null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2",user));
        }
        revision = strRevision;
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentStatusCommandDescription",user);
    }

}
