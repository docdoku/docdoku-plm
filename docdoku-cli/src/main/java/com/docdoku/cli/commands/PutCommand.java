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
import com.docdoku.core.common.Version;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

public class PutCommand extends AbstractCommandLine{


    @Option(name="-v", required = true, aliases = "--version", usage="specify revision of the part to save ('A', 'B'...)")
    private Version revision;

    @Argument(metaVar = "<partnumber>", required = true, index=0, usage = "the part number of the part to save")
    private String partNumber;

    @Argument(metaVar = "<cadfile>", required = true, index=1, usage = "specify the native cad file of the part to import")
    private File cadFile;

    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartRevisionKey partRPK = new PartRevisionKey(workspace,partNumber,revision.toString());

        PartRevision pr = productS.getPartRevision(partRPK);
        PartIteration pi = pr.getLastIteration();
        PartIterationKey partIPK = new PartIterationKey(partRPK, pi.getIteration());

        FileHelper fh = new FileHelper(user,password);
        fh.uploadNativeCADFile(getServerURL(), cadFile, partIPK);
    }

    @Override
    public String getDescription() {
        return "Save the current local copy of the cad file to the server. The part will remain checked out.";
    }
}
