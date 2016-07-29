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

import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentIterationKey;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.DocumentRevisionKey;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.AccountsManager;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentPutCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the document to save ('A', 'B'...); if not specified the document identity (id and revision) corresponding to the file will be selected")
    private String revision;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the document to save; if not specified choose the document corresponding to the file if it has already been imported")
    private String id;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Argument(metaVar = "<file>", required = true, index=0, usage = "specify the file of the document to import")
    private File file;

    @Override
    public void execImpl() throws Exception {
        if(id==null || revision==null){
            loadMetadata();
        }

        DocumentRevisionKey docRPK = new DocumentRevisionKey();
        docRPK.setWorkspaceId(workspace);
        docRPK.setDocumentMasterId(id);
        docRPK.setVersion(revision);

        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace,id,revision);
        DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);

        DocumentIterationKey docIPK = new DocumentIterationKey();
        docIPK.setWorkspaceId(workspace);
        docIPK.setDocumentMasterId(id);
        docIPK.setDocumentRevisionVersion(revision);
        docIPK.setIteration(di.getIteration());

        FileHelper fh = new FileHelper(user,password,output, new AccountsManager().getUserLocale(user));
        fh.uploadDocumentFile(getServerURL(), file, docIPK);
    }

    private void loadMetadata() throws IOException {
        MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
        String filePath = file.getAbsolutePath();
        id = meta.getDocumentId(filePath);
        String strRevision = meta.getRevision(filePath);
        if(id==null || strRevision==null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2",user));
        }
        revision = strRevision;
    }
    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentPutCommandDescription", user);
    }
}
