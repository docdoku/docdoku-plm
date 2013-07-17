/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

package com.docdoku.cli.commands;


import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.exceptions.DplmException;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.JSONPrinter;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Version;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

public class CheckInCommand extends AbstractCommandLine{


    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to check in ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private Version revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to check in; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile>] | <dir>]", index=0, usage = "specify the cad file of the part to check in or the path where cad files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-upload", usage="do not upload the cad file of the part if any")
    private boolean noUpload;

    @Option(name="-R", aliases = "--recursive", usage="execute the command through the product structure hierarchy")
    private boolean recursive;

    @Option(name="-j", aliases = "--jsonparser", usage="return a JSON description of the status part")
    private boolean jsonParser;

    public void execImpl() throws Exception {
        try {
            if(partNumber==null || revision==null){
                loadMetadata();
            }

            IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
            PartRevisionKey partRPK = new PartRevisionKey(workspace,partNumber,revision.toString());

            if(!noUpload){
                PartRevision pr = productS.getPartRevision(partRPK);
                if (pr == null) {
                    if (jsonParser) {
                        throw new DplmException("Part not found");
                    }
                }
                PartIteration pi = pr.getLastIteration();

                BinaryResource bin = pi.getNativeCADFile();
                if(bin!=null){
                    String fileName =  bin.getName();
                    File localFile = new File(path,fileName);
                    if(localFile.exists()){
                        PartIterationKey partIPK = new PartIterationKey(partRPK, pi.getIteration());
                        FileHelper fh = new FileHelper(user,password);
                        fh.uploadNativeCADFile(getServerURL(), localFile, partIPK);
                        localFile.setWritable(false);
                    }
                }
            }

            PartRevision pr = productS.checkInPart(partRPK);
            PartIteration pi = pr.getLastIteration();
            System.out.println("Checking in part: " + partNumber + " " + pr.getVersion() + "." + pi.getIteration() + " (" + workspace + ")");
        } catch (DplmException de) {
            JSONPrinter.printException(de);
        }
    }

    private void loadMetadata() throws IOException {
        if(path.isDirectory()){
            throw new IllegalArgumentException("<partnumber> or <revision> are not specified and the supplied path is not a file");
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        partNumber = meta.getPartNumber(filePath);
        String strRevision = meta.getRevision(filePath);
        if(partNumber==null || strRevision==null){
            throw new IllegalArgumentException("<partnumber> or <revision> are not specified and cannot be inferred from file");
        }
        revision = new Version(strRevision);
        //once partNumber and revision have been inferred, set path to folder where files are stored
        //in order to implement perform the rest of the treatment
        path=path.getParentFile();
    }

    @Override
    public String getDescription() {
        return "Perform a check in operation in order to validate the working copy of the part and hence make it visible to the whole team.";
    }
}
