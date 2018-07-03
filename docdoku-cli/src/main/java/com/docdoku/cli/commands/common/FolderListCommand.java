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

package com.docdoku.cli.commands.common;

import com.docdoku.api.models.FolderDTO;
import com.docdoku.api.services.FoldersApi;
import com.docdoku.cli.commands.BaseCommandLine;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Morgan Guimard
 */
public class FolderListCommand extends BaseCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-f", aliases = "--folder", usage="remote folder to list sub folders, default is workspace root folder")
    private String folder = null;

    @Override
    public void execImpl() throws Exception {
        FoldersApi foldersApi = new FoldersApi(client);
        String decodedPath = folder == null ? workspace : workspace+":"+folder;
        List<FolderDTO> folders = foldersApi.getSubFolders(workspace,decodedPath);
        output.printFolders(folders);
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("FolderListCommandDescription");
    }
}
