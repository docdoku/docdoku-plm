package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.PartApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 * @author Jean-Luc Mounsamy
 */
public class UndoCheckoutCommand extends BaseCommandLine {
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
            usage = "the id of the target to undo check out; if not specified choose the target corresponding to the file"
    )
    private String id;

    @Option(
            metaVar = "<revision>",
            name = "-r",
            aliases = "--revision",
            usage = "specify revision of the target to undo check out ('A', 'B'...); if not specified the target " +
                    "identity (id and revision) corresponding to the file will be selected"
    )
    private String revision;

    @Argument(
            metaVar = "[<file> | <dir>]",
            index = 0,
            usage = "specify the file of the target to undo check out or the path where files are stored (default is working directory)"
    )
    private File path = new File(System.getProperty("user.dir"));

    @Option(
            name = "-d",
            aliases = "--download",
            usage = "download the previous files of the target if any to revert the local copy"
    )
    private boolean download;

    @Option(
            name = "-f",
            aliases = "--force",
            usage = "overwrite existing files even if they have been modified locally"
    )
    private boolean force;

    @Override
    public void execImpl() throws Exception {
        try {
            if(!document && !part) {
                throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentOrPartOptionMandatory"));
            }
            if (id == null || revision == null) {
                loadMetadata();
            }
            if(document) {
                processDocument();
            } else if(part) {
                processPart();
            }
        } catch (Exception e) {
            output.printException(e);
            output.printCommandUsage(this);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("UndoCheckOutCommandDescription");
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
        if (id == null || strRevision == null) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("IdOrRevisionNotSpecified2"));
        }
        revision = strRevision;
        path = path.getParentFile();
    }

    private void processDocument() throws ApiException, IOException {
        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.undoCheckOutDocument(workspace, id, revision);
        DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);

        output.printInfo(langHelper.getLocalizedMessage("UndoCheckoutDocument") + " : " + id + "-" + dr.getVersion() + "-" + di.getIteration() + 1 + " (" + workspace + ")");

        if (download && !di.getAttachedFiles().isEmpty()) {
            FileHelper fh = new FileHelper(client, output, langHelper);
            fh.downloadDocumentFiles(path, user, workspace, id, dr, di, force);
        }
    }

    private void processPart() throws ApiException {
        PartApi partApi = new PartApi(client);
        PartRevisionDTO pr = partApi.undoCheckOut(workspace, id, revision);
        PartIterationDTO pi = LastIterationHelper.getLastIteration(pr);
        output.printInfo(langHelper.getLocalizedMessage("UndoCheckoutPart") + " : " + id + " " + pr.getVersion() + "." + pi.getIteration() + 1 + " (" + workspace + ")");

        BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();

        if (nativeCADFile != null && download) {
            FileHelper fh = new FileHelper(client, output, langHelper);
            fh.downloadPartFile(path, workspace, id, revision, pi.getIteration(), nativeCADFile.getName(), pr.getType(), "nativecad", force);
        }
    }
}
