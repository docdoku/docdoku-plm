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
import com.docdoku.core.product.*;
import com.docdoku.core.services.*;
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
public class CheckOutCommand extends AbstractCommandLine{

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to check out ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private Version revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to check out; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile>] | <dir>]", index=0, usage = "specify the cad file of the part to check out or the path where cad files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-n", aliases = "--no-download", usage="do not download the native cad file of the part if any")
    private boolean noDownload;

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
        if(partNumber==null || revision==null){
            loadMetadata();
        }
        productS = ScriptingTools.createProductService(getServerURL(), user, password);

        String strRevision = revision==null?null:revision.toString();

        ConfigSpec cs = null;

        if(baselineId != 0){
            cs = new BaselineConfigSpec(productS.getBaselineById(baselineId));
        }

        checkoutPart(partNumber,strRevision,0,cs);
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
        //once partNumber and revision have been inferred, set path to folder where files are stored
        //in order to implement perform the rest of the treatment
        path=path.getParentFile();
    }


    private void checkoutPart(String pPartNumber, String pRevision, int pIteration, ConfigSpec cs) throws IOException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartMasterNotFoundException, PartRevisionNotFoundException, LoginException, NoSuchAlgorithmException, PartIterationNotFoundException, NotAllowedException, FileAlreadyExistsException, AccessRightException, CreationException {

        PartIteration pi;
        PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, pPartNumber));
        PartRevision pr = productS.checkOutPart(new PartRevisionKey(workspace, pPartNumber, pRevision));

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

        if(bin!=null && !noDownload){
            FileHelper fh = new FileHelper(user,password);
            fh.downloadNativeCADFile(getServerURL(), path, workspace, pPartNumber, pr, pi, force);
        }

        if(recursive){
            PartIterationKey partIPK = new PartIterationKey(workspace,pPartNumber,pr.getVersion(),pi.getIteration());
            List<PartUsageLink> usageLinks = productS.getComponents(partIPK);

            for(PartUsageLink link:usageLinks){
                PartMaster subPM = link.getComponent();
                checkoutPart(subPM.getNumber(), null, 0, cs);
            }
        }

    }

    @Override
    public String getDescription() {
        return "Perform a check out operation and thus reserve the part for modification.";
    }
}
