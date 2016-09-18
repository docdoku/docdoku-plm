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
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
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
 * @author Florent Garin
 */
public class PartCheckOutCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to check out ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private String revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to check out; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile> | <dir>]", index=0, usage = "specify the cad file of the part to check out or the path where cad files are stored (default is working directory)")
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

    @Override
    public void execImpl() throws Exception {
        if(partNumber==null || revision==null){
            loadMetadata();
        }

        String strRevision = revision==null?null:revision;
        checkoutPart(partNumber,strRevision);
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

    private void checkoutPart(String pPartNumber, String pRevision) throws IOException, ApiException, LoginException, NoSuchAlgorithmException {

        PartsApi partsApi = new PartsApi(client);
        PartApi partApi = new PartApi(client);

        Locale locale = new AccountsManager().getUserLocale(user);

        //PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, pPartNumber));
        PartRevisionDTO pr;
        PartIterationDTO pi;

        output.printInfo(LangHelper.getLocalizedMessage("CheckingOutPart",locale)+ " : "+ pPartNumber);

        if(baselineId != 0){
            pi = partsApi.filterPartMasterInBaseline(workspace, pPartNumber,String.valueOf(baselineId));
            pr = partsApi.getPartRevision(workspace, pPartNumber, pi.getVersion());
        }else {
            pr = partsApi.getPartRevision(workspace, pPartNumber, pRevision);
            pi = LastIterationHelper.getLastIteration(pr);
        }

        if(pr.getCheckOutUser() == null) {
            try{
                pr = partApi.checkOut(workspace, pPartNumber, pr.getVersion(),"");
                pi = LastIterationHelper.getLastIteration(pr);
            }catch (Exception e){
                output.printException(e);
            }
        }

        BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();

        if(nativeCADFile!=null && !noDownload){
            FileHelper fh = new FileHelper(user,password,output,locale);
            fh.downloadNativeCADFile(getServerURL(), path, workspace, pPartNumber, pr, pi, force);
        }

        if(recursive){

            for(PartUsageLinkDTO link:pi.getComponents()){
                checkoutPart(link.getComponent().getNumber(), null);
            }
        }

    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartCheckOutCommandDescription",user);
    }
}
