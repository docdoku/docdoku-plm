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
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Morgan Guimard
 */
public class PartCreationCommand extends BaseCommandLine {

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", required = true, usage = "the part number of the part to save")
    private String partNumber;

    @Option(metaVar = "<partname>", name = "-N", aliases = "--partname", usage = "the part name of the part to save")
    private String partName;

    @Option(metaVar = "<description>", name = "-d", aliases = "--description", usage = "the description of the part to save")
    private String description;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-s", aliases = "--standard", metaVar = "<format>", usage="save as standard part")
    protected boolean standardPart = false;

    @Argument(metaVar = "<cadfile>", required = true, index=0, usage = "specify the cad file of the part to import")
    private File cadFile;

    @Override
    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartMaster partMaster = productS.createPartMaster(workspace, partNumber, partName, standardPart, null, description, null, null, null, null);
        PartRevision pr = partMaster.getLastRevision();
        PartRevisionKey partRPK = new PartRevisionKey(workspace, partNumber, pr.getVersion());
        PartIterationKey partIPK = new PartIterationKey(partRPK, pr.getLastIteration().getIteration());
        FileHelper fh = new FileHelper(user,password,output,new AccountsManager().getUserLocale(user));
        fh.uploadNativeCADFile(getServerURL(), cadFile, partIPK);
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartCreationCommandDescription",user);
    }
}
