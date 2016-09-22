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

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.DocumentRevisionKey;
import com.docdoku.api.models.UserDTO;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.AccountsManager;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentCheckOutCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the document to check out ('A', 'B'...); if not specified the document identity (id and revision) corresponding to the file will be selected")
    private String revision;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the document to check out; if not specified choose the document corresponding to the file")
    private String id;

    @Argument(metaVar = "[<file> | <dir>]", index=0, usage = "specify the file of the document to check out or the path where files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-download", usage="do not download the files of the document if any")
    private boolean noDownload;

    @Option(name="-f", aliases = "--force", usage="overwrite existing files even if they have been modified locally")
    private boolean force;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Override
    public void execImpl() throws Exception {
        if(id==null || revision==null){
            loadMetadata();
        }

        String strRevision = revision==null?null:revision;
        checkoutDocument(id, strRevision);
    }

    private void loadMetadata() throws IOException {
        if(path.isDirectory()){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified1",user));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        id = meta.getDocumentId(filePath);
        String strRevision = meta.getRevision(filePath);
        if(id==null || strRevision==null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2",user));
        }
        revision = strRevision;
        path=path.getParentFile();
    }

    private void checkoutDocument(String id, String pRevision) throws IOException, ApiException, LoginException, NoSuchAlgorithmException {

        Locale locale = new AccountsManager().getUserLocale(user);

        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey();
        documentRevisionKey.setWorkspaceId(workspace);
        documentRevisionKey.setDocumentMasterId(id);
        documentRevisionKey.setVersion(pRevision);

        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace,id,pRevision);
        DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);

        output.printInfo(
                LangHelper.getLocalizedMessage("CheckingOutDocument",locale)
                        + " : "
                        + id + "-" + dr.getVersion() + "-" + di.getIteration() + " (" + workspace + ")");

        UserDTO checkOutUser = dr.getCheckOutUser();

        if(checkOutUser == null) {
            try{
                dr = documentApi.checkOutDocument(workspace,id,pRevision,"");
                di = LastIterationHelper.getLastIteration(dr);
            }catch (ApiException e){
                output.printException(e);
            }
        }

        if(!noDownload && !di.getAttachedFiles().isEmpty()){
            FileHelper fh = new FileHelper(user,password,output,locale);
            fh.downloadDocumentFiles(getServerURL(), path, workspace, id, dr, di, force);
        }

    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentCheckOutCommandDescription",user);
    }
}
