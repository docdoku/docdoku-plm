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
import com.docdoku.cli.helpers.JSONOutput;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Option;

import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
public class PartListCommand extends AbstractCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-c", aliases = "--count", usage="return part master count in workspace")
    private boolean count;

    @Option(name="-s", aliases = "--start", usage="start offset")
    private int start;

    @Option(name="-m", aliases = "--max-results", usage="max results")
    private int max;

    @Override
    public Object execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);

        if(count){
            int partMastersCount = productS.getTotalNumberOfParts(workspace);
            return JSONOutput.printPartMastersCount(partMastersCount);
        }else{
            List<PartMaster> partMasters = productS.getPartMasters(workspace, start, max);
            return JSONOutput.printPartMasters(partMasters);
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
