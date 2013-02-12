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
import com.docdoku.core.common.Version;

import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;

public class GetCommand extends AbstractCommandLine{


    @Option(name="-r", aliases = "--revision", usage="specify revision of the part to retrieve ('A', 'B'...); default is the latest")
    private Version revision;

    @Option(name="-i", aliases = "--iteration", metaVar = "<iteration>", usage="specify iteration of the part to retrieve ('1','2', '24'...); default is the latest")
    private int iteration;

    @Argument(metaVar = "<partnumber>", required = true, index=0, usage = "the part number of the part to download")
    private String partNumber;

    @Argument(metaVar = "[<path>]", index=1, usage = "specify where to place downloaded files; if path is omitted, the working directory is used")
    private File path = new File(System.getProperty("user.dir"));

    public void execImpl() throws Exception {

        FileHelper fh = new FileHelper(user,password);
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);

        PartIterationKey partIPK = new PartIterationKey(workspace,partNumber,revision.toString(), iteration);
        //fh.downloadFile(cadFile,FileHelper.getPartURL(partIPK, fileName));
    }
}
