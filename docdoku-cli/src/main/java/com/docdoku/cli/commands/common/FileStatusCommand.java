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

package com.docdoku.cli.commands.common;

import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.cli.tools.ScriptingTools;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.document.DocumentRevisionKey;
import com.docdoku.core.exceptions.DocumentRevisionNotFoundException;
import com.docdoku.core.exceptions.PartRevisionNotFoundException;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IDocumentManagerWS;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Morgan Guimard
 */
public class FileStatusCommand extends BaseCommandLine {

    @Argument(metaVar = "[<file>]", required=true, index=0, usage = "specify the file of a document or a part to get a status")
    private File file;

    @Override
    public void execImpl() throws Exception {

        MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
        String filePath = file.getAbsolutePath();

        String workspace = meta.getWorkspace(filePath);
        long lastModified = meta.getLastModifiedDate(filePath);
        String strRevision = meta.getRevision(filePath);

        if(meta.isDocumentRelated(filePath)){
            String ref = meta.getDocumentId(filePath);
            try {
                IDocumentManagerWS documentS = ScriptingTools.createDocumentService(getServerURL(),user,password);
                DocumentRevision documentRevision = documentS.getDocumentRevision(new DocumentRevisionKey(workspace,ref,strRevision));
                output.printDocumentRevision(documentRevision,lastModified);
            } catch (DocumentRevisionNotFoundException e) {
                meta.deleteEntryInfo(file.getAbsolutePath());
                output.printException(e);
            }
        }
        else if(meta.isPartRelated(filePath)){
            String ref = meta.getPartNumber(filePath);
            try {
                IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
                PartRevision partRevision = productS.getPartRevision(new PartRevisionKey(workspace, ref, strRevision));
                output.printPartRevision(partRevision, lastModified);
            } catch (PartRevisionNotFoundException e) {
                meta.deleteEntryInfo(file.getAbsolutePath());
                output.printException(e);
            }
        }else{
            throw new IllegalArgumentException(LangHelper.getLocalizedMessage("FileNotIndexedException",user));
        }

    }


    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("FileStatusCommand",user);
    }

}
