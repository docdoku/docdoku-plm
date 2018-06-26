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

package com.docdoku.cli.helpers;

import com.docdoku.api.models.*;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import static com.docdoku.cli.helpers.CliOutput.formats.HUMAN;

/**
 * @author Morgan Guimard
 * @version 2.0, 14/11/14
 * @since   V2.0
 */
public abstract class CliOutput {
    protected boolean debug = false;

    public enum formats {
        HUMAN,
        JSON
    }
    public static CliOutput getOutput(formats pFormat, Locale pLocale) {
        formats format = pFormat;
        if(format == null){
            format = HUMAN;
        }
        switch(format){
            case HUMAN: return new HumanOutput(pLocale);
            case JSON: return new JSONOutput();
            default: return new HumanOutput(pLocale);
        }
    }

    public abstract void printException(Exception e);
    public abstract void printCommandUsage(CommandLine cl) throws IOException;
    public abstract void printUsage();
    public abstract void printInfo(String s);

    public abstract void print(String s);

    public abstract void printWorkspaces(List<WorkspaceDTO> workspaceDTOs);
    public abstract void printPartRevisionsCount(int partRevisionsCount);
    public abstract void printPartRevisions(List<PartRevisionDTO> partRevisions);
    public abstract void printBaselines(List<ProductBaselineDTO> productBaselines);
    public abstract void printPartRevision(PartRevisionDTO pr, long lastModified);
    public abstract void printConversion(ConversionDTO conversion);
    public abstract void printAccount(AccountDTO accountDTO);
    public abstract void printDocumentRevision(DocumentRevisionDTO dr, long lastModified);
    public abstract void printDocumentRevisions(List<DocumentRevisionDTO> documentRevisions);
    public abstract void printFolders(List<FolderDTO> folders);

    public abstract FilterInputStream getMonitor(long maximum, InputStream in);
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
