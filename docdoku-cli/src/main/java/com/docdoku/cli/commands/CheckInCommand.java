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
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.Version;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class CheckInCommand extends AbstractCommandLine{


    @Option(name="-v", required = true, aliases = "--version", usage="specify revision of the part to check in ('A', 'B'...)")
    private Version revision;

    @Argument(metaVar = "<partnumber>", required = true, index=0, usage = "the part number of the part to check in")
    private String partNumber;

    @Argument(metaVar = "[<path>]", index=1, usage = "specify where to look at the native cad files; if path is omitted, the working directory is used")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-upload", usage="do not upload the native cad file of the part if any")
    private boolean noUpload;

    @Option(name="-R", aliases = "--recursive", usage="execute the command through the product structure hierarchy")
    private boolean recursive;

    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartRevisionKey partRPK = new PartRevisionKey(workspace,partNumber,revision.toString());

        if(!noUpload){
            PartRevision pr = productS.getPartRevision(partRPK);
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


    }

    @Override
    public String getDescription() {
        return "Perform a check in operation in order to validate the working copy of the part and hence make it visible to the whole team.";
    }
}
