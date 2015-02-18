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

package com.docdoku.cli.commands;

import com.docdoku.cli.helpers.LangHelper;
import com.docdoku.core.common.Version;
import com.docdoku.core.configuration.ConfigSpec;
import com.docdoku.core.exceptions.*;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Morgan Guimard
 */
public class DocumentGetCommand extends AbstractCommandLine{

    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the document to retrieve ('A', 'B'...); default is the latest")
    private Version revision;

    @Option(name="-i", aliases = "--iteration", metaVar = "<iteration>", usage="specify iteration of the document to retrieve ('1','2', '24'...); default is the latest")
    private int iteration;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the document to fetch; if not specified choose the document corresponding to the file")
    private String id;

    @Argument(metaVar = "[<file>] | <dir>]", index=0, usage = "specify the cad file of the document to fetch or the path where cad files are stored (default is working directory)")
    private File path = new File(System.getProperty("user.dir"));

    @Option(name="-f", aliases = "--force", usage="overwrite existing files even if they have been modified locally")
    private boolean force;

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    public void execImpl() throws Exception {

    }

    private void loadMetadata() throws IOException {

    }

    private void getPart(String pPartNumber, String pRevision, int pIteration, ConfigSpec cs) throws IOException, UserNotFoundException, WorkspaceNotFoundException, UserNotActiveException, PartMasterNotFoundException, PartRevisionNotFoundException, LoginException, NoSuchAlgorithmException, PartIterationNotFoundException, NotAllowedException, AccessRightException {


    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("DocumentGetCommandDescription",user);
    }
}
