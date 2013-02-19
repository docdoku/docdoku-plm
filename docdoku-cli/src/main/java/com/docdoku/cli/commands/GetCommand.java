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

import java.io.Console;
import java.io.File;

public class GetCommand extends AbstractCommandLine{


    @Option(name="-v", aliases = "--version", usage="specify revision of the part to retrieve ('A', 'B'...); default is the latest")
    private Version revision;

    @Option(name="-i", aliases = "--iteration", metaVar = "<iteration>", usage="specify iteration of the part to retrieve ('1','2', '24'...); default is the latest")
    private int iteration;

    @Argument(metaVar = "<partnumber>", required = true, index=0, usage = "the part number of the part to fetch")
    private String partNumber;

    @Argument(metaVar = "[<path>]", index=1, usage = "specify where to place downloaded files; if path is omitted, the working directory is used")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-f", aliases = "--force", usage="overwrite existing files even if they have been modified locally")
    private boolean force;

    @Option(name="-R", aliases = "--recursive", usage="execute the command through the product structure hierarchy")
    private boolean recursive;


    public void execImpl() throws Exception {


        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartRevision pr;
        PartIteration pi;
        if(revision==null){
            PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, partNumber));
            pr = pm.getLastRevision();
        }else{
            pr = productS.getPartRevision(new PartRevisionKey(workspace,partNumber,revision.toString()));
        }
        if(iteration==0){
            pi = pr.getLastIteration();
        }else{
            if(iteration > pr.getNumberOfIterations()){
                throw new IllegalArgumentException("Iteration " + iteration + " doesn't exist");
            }else{
                pi = pr.getIteration(iteration);
            }
        }

        BinaryResource bin = pi.getNativeCADFile();
        if(bin==null){
            throw new IllegalArgumentException("No file for this part");
        }else{
            FileHelper fh = new FileHelper(user,password);
            fh.downloadNativeCADFile(getServerURL(), path, workspace, partNumber, pr, pi, force);
        }
    }


}
