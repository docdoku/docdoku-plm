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

import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.AccountsManager;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Version;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;
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
    private Version revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to check in; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile>] | <dir>]", index=0, usage = "specify the cad file of the part to check in or the path where cad files are stored (default is working directory)")
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

        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartRevisionKey partRPK = new PartRevisionKey(workspace,partNumber,revision.toString());
        PartRevision pr = productS.getPartRevision(partRPK);
        PartIteration pi = pr.getLastIteration();
        PartIterationKey partIPK = new PartIterationKey(partRPK, pi.getIteration());

        if(!noUpload){
            BinaryResource bin = pi.getNativeCADFile();
            if(bin!=null){
                String fileName =  bin.getName();
                File localFile = new File(path,fileName);
                if(localFile.exists()){

                    FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));
                    fh.uploadNativeCADFile(getServerURL(), localFile, partIPK);
                    localFile.setWritable(false);
                }
            }
        }

        if(message != null && !message.isEmpty()){
            productS.updatePartIteration(partIPK,message,null,null,null,null, null,null,null);
        }

        pr = productS.checkInPart(partRPK);
        pi = pr.getLastIteration();

        output.printInfo(LangHelper.getLocalizedMessage("CheckingInPart",user)  + " : " + partNumber + " " + pr.getVersion() + "." + pi.getIteration() + " (" + workspace + ")");
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
        revision = new Version(strRevision);
        //once partNumber and revision have been inferred, set path to folder where files are stored
        //in order to implement perform the rest of the treatment
        path=path.getParentFile();
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartCheckInCommandDescription",user);
    }
}
