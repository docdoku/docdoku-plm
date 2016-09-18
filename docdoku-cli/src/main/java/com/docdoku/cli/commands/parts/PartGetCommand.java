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

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.BinaryResourceDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.models.PartUsageLinkDTO;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
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

/**
 *
 * @author Florent Garin
 */
public class PartGetCommand extends BaseCommandLine {


    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to retrieve ('A', 'B'...); default is the latest")
    private String revision;

    @Option(name="-i", aliases = "--iteration", metaVar = "<iteration>", usage="specify iteration of the part to retrieve ('1','2', '24'...); default is the latest")
    private int iteration;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to fetch; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile> | <dir>]", index=0, usage = "specify the cad file of the part to fetch or the path where cad files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-f", aliases = "--force", usage="overwrite existing files even if they have been modified locally")
    private boolean force;

    @Option(name="-R", aliases = "--recursive", usage="execute the command through the product structure hierarchy")
    private boolean recursive;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-b", aliases = "--baseline", metaVar = "<baseline>", usage="baseline to filter")
    protected int baselineId;

    @Override
    public void execImpl() throws Exception {

        if(partNumber==null){
            loadMetadata();
        }

        String strRevision = revision==null?null:revision;


        getPart(partNumber, strRevision, iteration);
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
        //The part is inferred from the cad file, hence fetch the fresh (latest) iteration
        iteration=0;
        //once partNumber and revision have been inferred, set path to folder where files are stored
        //in order to implement perform the rest of the treatment
        path=path.getParentFile();
    }

    private void getPart(String pPartNumber, String pRevision, int pIteration) throws IOException, ApiException, LoginException, NoSuchAlgorithmException {

        // TODO may break => unused parameter pIteration ?? should review algorithm and command specs

        PartsApi partsApi = new PartsApi(client);
        PartApi partApi = new PartApi(client);

        PartRevisionDTO pr;
        PartIterationDTO pi;

        if(baselineId != 0){
            pi = partsApi.filterPartMasterInBaseline(workspace, pPartNumber, String.valueOf(baselineId));
            pr = partApi.getPartRevision(workspace,pPartNumber,pi.getVersion());
        }else {
            pr = partsApi.getPartRevision(workspace, pPartNumber, pRevision);
            pi = LastIterationHelper.getLastIteration(pr);
        }

        BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();

        if(nativeCADFile!=null){
            FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));
            fh.downloadNativeCADFile(getServerURL(), path, workspace, pPartNumber, pr, pi, force);
        }else{
            output.printInfo(LangHelper.getLocalizedMessage("NoFileForPart",user) + " : "  + pPartNumber + " " + pr.getVersion() + "." + pi.getIteration() + " (" + workspace + ")");
        }

        if(recursive){
            for(PartUsageLinkDTO link:pi.getComponents()){
                getPart(link.getComponent().getNumber(),null,0);
            }
        }

    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartGetCommandDescription",user);
    }
}
