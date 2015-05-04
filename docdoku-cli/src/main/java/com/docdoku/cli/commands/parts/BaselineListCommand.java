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
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
public class BaselineListCommand extends BaseCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(metaVar = "<partnumber>", required = true, name = "-o", aliases = "--part", usage = "the part number of the part to verify the existence of baselines")
    private String number;

    @Option(metaVar = "<revision>", required = true, name="-r", aliases = "--revision", usage="specify revision of the part to analyze ('A', 'B'...)")
    private String revision;

    private IProductManagerWS productS;

    @Override
    public void execImpl() throws Exception {
        productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartRevisionKey pK = new PartRevisionKey(workspace,number,revision);
        List<ProductBaseline> productBaselines = productS.findBaselinesWherePartRevisionHasIterations(pK);
        output.printBaselines(productBaselines);
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("BaselineListCommandDescription",user);
    }
}
