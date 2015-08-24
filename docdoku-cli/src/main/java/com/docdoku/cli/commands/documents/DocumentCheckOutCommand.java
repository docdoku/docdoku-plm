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
import com.docdoku.cli.helpers.AccountsManager;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.common.Version;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.services.IDocumentManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentCheckOutCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the document to check out ('A', 'B'...); if not specified the document identity (id and revision) corresponding to the file will be selected")
    private Version revision;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the document to check out; if not specified choose the document corresponding to the file")
    private String id;

    @Argument(metaVar = "[<file>] | <dir>]", index=0, usage = "specify the file of the document to check out or the path where files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-download", usage="do not download the files of the document if any")
    private boolean noDownload;

    @Option(name="-f", aliases = "--force", usage="overwrite existing files even if they have been modified locally")
    private boolean force;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;


    private IDocumentManagerWS documentS;

    @Override
    public void execImpl() throws Exception {
        if(id==null || revision==null){
            loadMetadata();
        }
        documentS = ScriptingTools.createDocumentService(getServerURL(), user, password);

        String strRevision = revision==null?null:revision.toString();

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
        revision = new Version(strRevision);
        path=path.getParentFile();
    }

    private void checkoutDocument(String id, String pRevision) throws IOException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, LoginException, NoSuchAlgorithmException, NotAllowedException, FileAlreadyExistsException, AccessRightException, CreationException, DocumentRevisionNotFoundException {

        DocumentRevisionKey documentRevisionKey = new DocumentRevisionKey(workspace, id, pRevision);

        DocumentRevision dr = documentS.getDocumentRevision(documentRevisionKey);
        DocumentIteration di = dr.getLastIteration();

        if(!dr.isCheckedOut()) {
            try{
                dr = documentS.checkOutDocument(documentRevisionKey);
                di = dr.getLastIteration();
            }catch (Exception e){
                output.printException(e);
            }
        }

        if(!noDownload && !di.getAttachedFiles().isEmpty()){
            FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));
            fh.downloadDocumentFiles(getServerURL(), path, workspace, id, dr, di, force);
        }

    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentCheckOutCommandDescription",user);
    }
}
