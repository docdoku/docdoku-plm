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
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.common.Version;
import com.docdoku.core.exceptions.PartMasterNotFoundException;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Florent Garin
 */
public class PartStatusCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to get a status ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private Version revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to get a status; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile>]", index=0, usage = "specify the cad file of the part to get a status")
    private File cadFile;

    @Option(name="-w", aliases = "--workspace", required = false, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    private long lastModified;

    @Override
    public void execImpl() throws Exception {
        try {

            if(partNumber==null || revision==null || workspace==null){
                loadMetadata();
            }

            IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);

            if(revision == null){
                PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, partNumber));
                output.printPartMaster(pm, lastModified);
            }else{
                PartRevision partRevision = productS.getPartRevision(new PartRevisionKey(workspace, partNumber, revision.toString()));
                output.printPartRevision(partRevision, lastModified);
            }

        } catch (PartMasterNotFoundException pmnfe) {

            MetaDirectoryManager meta = new MetaDirectoryManager(cadFile.getParentFile());
            meta.deleteEntryInfo(cadFile.getAbsolutePath());

            output.printException(pmnfe);

        }
    }

    private void loadMetadata() throws IOException {
        if(cadFile==null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("PartNumberOrRevisionNotSpecified1",user));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(cadFile.getParentFile());
        String filePath = cadFile.getAbsolutePath();
        partNumber = meta.getPartNumber(filePath);
        workspace = meta.getWorkspace(filePath);
        lastModified = meta.getLastModifiedDate(filePath);
        String strRevision = meta.getRevision(filePath);
        if(partNumber==null || strRevision==null || workspace == null){
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("PartNumberOrRevisionNotSpecified2",user));
        }
        revision = new Version(strRevision);
    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("PartStatusCommandDescription",user);
    }

}
