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

import com.docdoku.api.models.BinaryResourceDTO;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentIterationKey;
import com.docdoku.api.models.DocumentRevisionDTO;
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
public class DocumentCheckInCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the document to check in ('A', 'B'...); if not specified the document identity (id and revision) corresponding to the file will be selected")
    private String revision;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the document to check in; if not specified choose the document corresponding to the file")
    private String id;

    @Argument(metaVar = "[<file> | <dir>]", index=0, usage = "specify the file of the document to check in or the path where files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-upload", usage="do not upload the file of the document if any")
    private boolean noUpload;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(metaVar = "<message>", name = "-m", aliases = "--message", usage = "a message specifying the iteration modifications")
    private String message;

    @Override
    public void execImpl() throws Exception {

        if(id ==null || revision==null){
            loadMetadata();
        }

        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace,id,revision,null);

        DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);
        DocumentIterationKey docIPK = new DocumentIterationKey();
        docIPK.setWorkspaceId(workspace);
        docIPK.setDocumentMasterId(id);
        docIPK.setDocumentRevisionVersion(revision);
        docIPK.setIteration(di.getIteration());

        if(!noUpload && !di.getAttachedFiles().isEmpty()){

            for(BinaryResourceDTO binaryResourceDTO:di.getAttachedFiles()){
                String fileName =  binaryResourceDTO.getName();
                File localFile = new File(path,fileName);
                if(localFile.exists()){
                    FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));

                    fh.uploadDocumentFile(getServerURL(), localFile, docIPK);
                    localFile.setWritable(false, false);
                }
            }

        }

        if(message != null && !message.isEmpty()){
            di.setRevisionNote(message);
            documentApi.updateDocumentIteration(workspace, id, revision, String.valueOf(di.getIteration()), di);
        }

        output.printInfo(LangHelper.getLocalizedMessage("CheckingInDocument",user)  + " : " + id
                + "-" + dr.getVersion() + "-" + di.getIteration() + " (" + workspace + ")");

        documentApi.checkInDocument(workspace,id, revision,"");

    }

    private void loadMetadata() throws IOException {
        if(path.isDirectory()){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified1",user));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        id = meta.getDocumentId(filePath);
        String strRevision = meta.getRevision(filePath);
        if(id ==null || strRevision==null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2",user));
        }
        revision = strRevision;
        path=path.getParentFile();
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentCheckInCommandDescription",user);
    }
}
