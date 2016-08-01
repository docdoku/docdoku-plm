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

package com.docdoku.cli.commands.parts;

import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.PartApi;
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
 * @author Florent Garin
 */
public class PartCheckInCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to check in ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private String revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to check in; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile> | <dir>]", index=0, usage = "specify the cad file of the part to check in or the path where cad files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-upload", usage="do not upload the cad file of the part if any")
    private boolean noUpload;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(metaVar = "<message>", name = "-m", aliases = "--message", usage = "a message specifying the iteration modifications")
    private String message;

    @Override
    public void execImpl() throws Exception {

        if(partNumber==null || revision==null){
            loadMetadata();
        }

        PartApi partApi = new PartApi(client);

        PartRevisionDTO pr = partApi.getPartRevision(workspace,partNumber,revision);
        PartIterationDTO pi = LastIterationHelper.getLastIteration(pr);

        PartRevisionKey partRPK = new PartRevisionKey();
        partRPK.setWorkspaceId(workspace);
        partRPK.setPartMasterNumber(partNumber);
        partRPK.setVersion(revision);

        PartIterationKey partIPK = new PartIterationKey();
        partIPK.setWorkspaceId(workspace);
        partIPK.setPartMasterNumber(partNumber);
        partIPK.setPartRevisionVersion(revision);
        partIPK.setIteration(pi.getIteration());

        if(!noUpload){
            BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();
            if(nativeCADFile!=null){

                String fileName =  nativeCADFile.getName();
                File localFile = new File(path,fileName);
                if(localFile.exists()){
                    FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));
                    fh.uploadNativeCADFile(getServerURL(), localFile, partIPK);
                    localFile.setWritable(false,false);
                }
            }
        }

        if(message != null && !message.isEmpty()){
            pi.setIterationNote(message);
            partApi.updatePartIteration(workspace,partNumber,revision, pi.getIteration(), pi);
        }

        output.printInfo(LangHelper.getLocalizedMessage("CheckingInPart",user)  + " : " + partNumber + " " + pr.getVersion() + "." + pi.getIteration() + " (" + workspace + ")");

        partApi.checkIn(workspace,partNumber,revision,"");

    }

    private void loadMetadata() throws IOException {
        if(path.isDirectory()){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("PartNumberOrRevisionNotSpecified1",user));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        partNumber = meta.getPartNumber(filePath);
        String strRevision = meta.getRevision(filePath);
        if(partNumber==null || strRevision==null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("PartNumberOrRevisionNotSpecified2",user));
        }
        revision = strRevision;
        //once partNumber and revision have been inferred, set path to folder where files are stored
        //in order to implement perform the rest of the treatment
        path=path.getParentFile();
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartCheckInCommandDescription",user);
    }
}
