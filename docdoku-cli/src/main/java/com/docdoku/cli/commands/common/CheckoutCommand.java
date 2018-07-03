package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jean-Luc Mounsamy
 */
public class CheckoutCommand extends BaseCommandLine {
    @Option(
            name = "-doc",
            aliases = "--document",
            usage = "Use to specify target as document",
            forbids = {"-part"}
    )
    private boolean document;

    @Option(
            name = "-part",
            usage = "Use to specify target as part",
            forbids = {"-doc"}
    )
    private boolean part;

    @Option(
            name = "-w",
            aliases = "--workspace",
            required = true,
            metaVar = "<workspace>",
            usage = "workspace on which operations occur"
    )
    protected String workspace;

    @Option(
            metaVar = "<id>",
            name = "-o",
            aliases = "--id",
            usage = "the id of the target to check out; if not specified choose the target corresponding to the file"
    )
    private String id;

    @Option(
            metaVar = "<revision>",
            name = "-r",
            aliases = "--revision",
            usage = "specify revision of the target to check out ('A', 'B'...); if not specified the target " +
                    "identity (id and revision) corresponding to the file will be selected"
    )
    private String revision;

    @Argument(
            metaVar = "[<file> | <dir>]",
            index = 0,
            usage = "specify the file of the target to check out or the path where files are stored (default is working directory)"
    )
    private File path = new File(System.getProperty("user.dir"));

    @Option(
            name = "-n",
            aliases = "--no-download",
            usage = "do not download the files of the target if any"
    )
    private boolean noDownload;

    @Option(
            name = "-f",
            aliases = "--force",
            usage = "overwrite existing files even if they have been modified locally"
    )
    private boolean force;

    @Option(
            name = "-R",
            aliases = "--recursive",
            usage = "execute the command through the product structure hierarchy",
            forbids = {"-doc"}
    )
    private boolean recursive;

    @Option(
            name = "-b",
            aliases = "--baseline",
            metaVar = "<baseline>",
            usage = "baseline to filter",
            forbids = {"-doc"}
    )
    protected Integer baselineId;

    private Set<String> alreadyProcessed = new HashSet<>();

    @Override
    public void execImpl() throws Exception {
        try {
            if(!document && !part)
                throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentOrPartOptionMandatory"));
            if (id == null || revision == null)
                loadMetadata();
            if(document)
                processDocument();
            else if(part)
                processPart(id, revision);
        } catch (Exception e) {
            output.printException(e);
            output.printCommandUsage(this);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("CheckOutCommandDescription");
    }

    private void loadMetadata() throws IOException {
        if (path.isDirectory()) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("IdOrRevisionNotSpecified1"));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        if(document)
            id = meta.getDocumentId(filePath);
        else if(part)
            id = meta.getPartNumber(filePath);
        String strRevision = meta.getRevision(filePath);
        if (id == null || strRevision == null)
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("IdOrRevisionNotSpecified2"));
        revision = strRevision;
        path = path.getParentFile();
    }

    private void processDocument() throws Exception {
        DocumentRevisionDTO documentRevisionKey = new DocumentRevisionDTO();
        documentRevisionKey.setWorkspaceId(workspace);
        documentRevisionKey.setDocumentMasterId(id);
        documentRevisionKey.setVersion(revision);

        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace, id, revision);
        DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);

        output.printInfo(
                langHelper.getLocalizedMessage("CheckingOutDocument")
                        + " : "
                        + id + "-" + dr.getVersion() + "-" + di.getIteration() + " (" + workspace + ")");

        UserDTO checkOutUser = dr.getCheckOutUser();

        if (checkOutUser == null) {
            try {
                dr = documentApi.checkOutDocument(workspace, id, revision);
                di = LastIterationHelper.getLastIteration(dr);
            } catch (ApiException e) {
                output.printException(e);
            }
        }

        if (!noDownload && !di.getAttachedFiles().isEmpty()) {
            FileHelper fh = new FileHelper(client, output, langHelper);
            fh.downloadDocumentFiles(path, user, workspace, id, dr, di, force);
        }
    }

    private void processPart(String pPartNumber, String pRevision) throws Exception {
        if (alreadyProcessed.contains(pPartNumber)) {
            return;
        }
        PartsApi partsApi = new PartsApi(client);
        PartApi partApi = new PartApi(client);
        PartRevisionDTO pr;
        PartIterationDTO pi;
        output.printInfo(langHelper.getLocalizedMessage("CheckingOutPart") + " : " + pPartNumber);
        if (pRevision != null) {
            pr = partsApi.getPartRevision(workspace, pPartNumber, pRevision);
            pi = LastIterationHelper.getLastIteration(pr);
        } else if (baselineId != null) {
            pi = partsApi.filterPartMasterInBaseline(workspace, pPartNumber, baselineId);
            pr = partsApi.getPartRevision(workspace, pPartNumber, pi.getVersion());
        } else {
            pr = partsApi.getLatestPartRevision(workspace, pPartNumber);
            pi = LastIterationHelper.getLastIteration(pr);
        }

        if (pr.getCheckOutUser() == null) {
            pr = partApi.checkOut(workspace, pPartNumber, pr.getVersion());
            pi = LastIterationHelper.getLastIteration(pr);
        }
        BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();

        if (nativeCADFile != null && !noDownload) {
            FileHelper fh = new FileHelper(client, output, langHelper);
            fh.downloadPartFile(path, workspace, pPartNumber, pRevision, pi.getIteration(), nativeCADFile.getName(), pr.getType(), "nativecad", force);
        }
        alreadyProcessed.add(pPartNumber);
        if (recursive) {
            for (PartUsageLinkDTO link : pi.getComponents()) {
                String linkNumber = link.getComponent().getNumber();
                processPart(linkNumber, null);
            }
        }
    }
}
