/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.PartApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 * @author Florent Garin
 */
public class PartPutCommand extends BaseCommandLine {

    @Option(metaVar = "<revision>", name = "-r", aliases = "--revision", usage = "specify revision of the part to save ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private String revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to save; if not specified choose the part corresponding to the cad file if it has already been imported")
    private String partNumber;

    @Option(name = "-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage = "workspace on which operations occur")
    protected String workspace;

    @Argument(metaVar = "<cadfile>", required = true, index = 0, usage = "specify the cad file of the part to import")
    private File cadFile;

    @Override
    public void execImpl() throws Exception {
        if (partNumber == null || revision == null) {
            loadMetadata();
        }

        PartRevisionDTO partRPK = new PartRevisionDTO();
        partRPK.setWorkspaceId(workspace);
        partRPK.setNumber(partNumber);
        partRPK.setVersion(revision);

        PartApi partApi = new PartApi(client);
        PartRevisionDTO pr = partApi.getPartRevision(workspace, partNumber, revision);
        PartIterationDTO pi = LastIterationHelper.getLastIteration(pr);

        PartIterationDTO partIPK = new PartIterationDTO();
        partIPK.setWorkspaceId(workspace);
        partIPK.setNumber(partNumber);
        partIPK.setVersion(revision);
        partIPK.setIteration(pi.getIteration());

        FileHelper fh = new FileHelper(user, password, output, langHelper);
        fh.uploadNativeCADFile(getServerURL(), cadFile, partIPK);
    }

    private void loadMetadata() throws IOException {
        MetaDirectoryManager meta = new MetaDirectoryManager(cadFile.getParentFile());
        String filePath = cadFile.getAbsolutePath();
        partNumber = meta.getPartNumber(filePath);
        String strRevision = meta.getRevision(filePath);
        if (partNumber == null || strRevision == null) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("PartNumberOrRevisionNotSpecified2"));
        }
        revision = strRevision;
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("PartPutCommandDescription");
    }
}
