package com.docdoku.cli.commands;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.core.common.Version;
import com.docdoku.core.product.*;
import com.docdoku.core.security.ACLUserEntry;
import com.docdoku.core.security.ACLUserGroupEntry;
import com.docdoku.core.services.IProductManagerWS;
import com.docdoku.core.util.FileIO;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

public class PartCreationCommand extends AbstractCommandLine {

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", required = true, usage = "the part number of the part to save")
    private String partNumber;

    @Option(metaVar = "<partname>", name = "-N", aliases = "--partname", usage = "the part name of the part to save")
    private String partName;

    @Option(metaVar = "<description>", name = "-d", aliases = "--description", usage = "the description of the part to save")
    private String description;

    @Argument(metaVar = "<cadfile>", required = true, index=0, usage = "specify the cad file of the part to import")
    private File cadFile;

    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartMaster partMaster = productS.createPartMaster(workspace, partNumber, partName, description, false, null, "", null, null, null, null);
        PartRevision pr = partMaster.getLastRevision();
        PartRevisionKey partRPK = new PartRevisionKey(workspace, partNumber, pr.getVersion());
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
