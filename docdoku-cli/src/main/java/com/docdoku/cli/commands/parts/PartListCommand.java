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
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
public class PartListCommand extends BaseCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-c", aliases = "--count", usage="return the number of part revisions within the workspace")
    private boolean count;

    @Option(name="-s", aliases = "--start", usage="start offset")
    private int start;

    @Option(name="-m", aliases = "--max-results", usage="max results")
    private int max;

    @Override
    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        if(count){
            int partRevisionsCount = productS.getPartsInWorkspaceCount(workspace);
            output.printPartRevisionsCount(partRevisionsCount);
        }else{
            List<PartRevision> partRevisions = productS.getPartRevisions(workspace, start, max);
            output.printPartRevisions(partRevisions);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartListCommandDescription",user);
    }
}
