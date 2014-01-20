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
import com.docdoku.core.configuration.BaselineConfigSpec;
import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *
 * @author Florent Garin
 */
public class GetCommand extends AbstractCommandLine{


    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to retrieve ('A', 'B'...); default is the latest")
    private Version revision;

    @Option(name="-i", aliases = "--iteration", metaVar = "<iteration>", usage="specify iteration of the part to retrieve ('1','2', '24'...); default is the latest")
    private int iteration;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to fetch; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile>] | <dir>]", index=0, usage = "specify the cad file of the part to fetch or the path where cad files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-f", aliases = "--force", usage="overwrite existing files even if they have been modified locally")
    private boolean force;

    @Option(name="-R", aliases = "--recursive", usage="execute the command through the product structure hierarchy")
    private boolean recursive;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-b", aliases = "--baseline", metaVar = "<baseline>", usage="baseline to filter")
    protected int baselineId;

    private IProductManagerWS productS;

    public Object execImpl() throws Exception {

        if(partNumber==null){
            loadMetadata();
        }

        productS = ScriptingTools.createProductService(getServerURL(), user, password);
        String strRevision = revision==null?null:revision.toString();

        ConfigSpec cs = null;

        if(baselineId != 0){
            cs = new BaselineConfigSpec(productS.getBaselineById(baselineId));
        }

        getPart(partNumber, strRevision, iteration, cs);
        return null;
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
        //The part is inferred from the cad file, hence fetch the fresh (latest) iteration
        iteration=0;
        //once partNumber and revision have been inferred, set path to folder where files are stored
        //in order to implement perform the rest of the treatment
        path=path.getParentFile();
    }

    private void getPart(String pPartNumber, String pRevision, int pIteration,ConfigSpec cs) throws IOException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartMasterNotFoundException, PartRevisionNotFoundException, LoginException, NoSuchAlgorithmException, PartIterationNotFoundException, NotAllowedException {
        PartRevision pr;
        PartIteration pi;
        PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, pPartNumber));
        if(pRevision==null){
            pr = pm.getLastRevision();
        }else{
            pr = productS.getPartRevision(new PartRevisionKey(workspace,pPartNumber,pRevision));
        }

        if(cs != null){
            pi = cs.filterConfigSpec(pm);
        }else if(pIteration==0){
            pi = pr.getLastIteration();
        }else{
            if(pIteration > pr.getNumberOfIterations()){
                throw new IllegalArgumentException("Iteration " + pIteration + " doesn't exist");
            }else{
                pi = pr.getIteration(pIteration);
            }
        }

        BinaryResource bin = pi.getNativeCADFile();

        if(bin!=null){
            FileHelper fh = new FileHelper(user,password);
            fh.downloadNativeCADFile(getServerURL(), path, workspace, pPartNumber, pr, pi, force);
        }else{
            System.out.println("No file for part: "  + pPartNumber + " " + pr.getVersion() + "." + pi.getIteration() + " (" + workspace + ")");
        }

        if(recursive){
            PartIterationKey partIPK = new PartIterationKey(workspace,pPartNumber,pr.getVersion(),pi.getIteration());
            List<PartUsageLink> usageLinks = productS.getComponents(partIPK);

            for(PartUsageLink link:usageLinks){
                PartMaster subPM = link.getComponent();
                getPart(subPM.getNumber(),null,0,cs);
            }
        }

    }

    @Override
    public String getDescription() {
        return "Retrieve the cad file of the given part as well as its sub-components if the command is performed recursively.";
    }
}
