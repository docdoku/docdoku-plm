package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.FileHelper;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jean-Luc Mounsamy
 */
public class GetCommand extends BaseCommandLine {
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
            name="-w",
            aliases = "--workspace",
            required = true,
            metaVar = "<workspace>",
            usage="workspace on which operations occur"
    )
    protected String workspace;

    @Option(
            metaVar = "<id>",
            name = "-o",
            aliases = "--id",
            usage = "the id of the target to fetch; if not specified choose the target corresponding to the file"
    )
    private String id;

    @Option(
            metaVar = "<revision>",
            name="-r",
            aliases = "--revision",
            usage="specify revision of the target to retrieve ('A', 'B'...); default is the latest"
    )
    private String revision;

    @Option(
            name="-i",
            aliases = "--iteration",
            metaVar = "<iteration>",
            usage="specify iteration of the target to retrieve ('1','2', '24'...); default is the latest"
    )
    private int iteration;

    @Argument(
            metaVar = "[<file> | <dir>]",
            index=0,
            usage = "specify the file of the target to fetch or the path where files are stored (default is working directory)"
    )
    private File path = new File(System.getProperty("user.dir"));

    @Option(
            name="-f",
            aliases = "--force",
            usage="overwrite existing files even if they have been modified locally"
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

    @Override
    public void execImpl() throws Exception {
        try {
            if(!document && !part) {
                throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentOrPartOptionMandatory"));
            }
            if(id == null) {
                loadMetadata();
            }
            if(document) {
                getDocument(id, revision, iteration);
            } else if(part) {
                getPart(id, revision, iteration, new HashSet<>());
            }
        } catch (Exception e) {
            output.printException(e);
            output.printCommandUsage(this);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("GetCommandDescription");
    }

    private void loadMetadata() throws IOException {
        if(path.isDirectory()){
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified1"));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        if(document) {
            id = meta.getDocumentId(filePath);
        } else if(part) {
            id = meta.getPartNumber(filePath);
        }
        String strRevision = meta.getRevision(filePath);
        if(id==null || strRevision==null) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2"));
        }
        revision = strRevision;
        iteration=0;
        path=path.getParentFile();
    }

    private void getDocument(String pId, String pRevision, int pIteration) throws IOException, ApiException {
        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace,pId,pRevision);
        DocumentIterationDTO di;
        if(pIteration == 0) {
            di = LastIterationHelper.getLastIteration(dr);
        } else if(pIteration > dr.getDocumentIterations().size()) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("IterationNotExisting"));
        } else {
            di = dr.getDocumentIterations().get(pIteration-1);
        }
        if(di.getAttachedFiles().isEmpty()) {
            output.printInfo(langHelper.getLocalizedMessage("NoFilesForDocument") + " : "  + id + " " + dr.getVersion() + "." + di.getIteration() + " (" + workspace + ")");
        } else {
            FileHelper fh = new FileHelper(client, output, langHelper);
            List<File> files = fh.downloadDocumentFiles(path, user, workspace, id, dr, di, force);
            output.printInfo(files.toString());
        }
    }

    private void getPart(String pPartNumber, String pRevision, Integer pIteration, Set<String> alreadyProcessed) throws ApiException {
        if (alreadyProcessed.contains(pPartNumber)) {
            return;
        }
        PartsApi partsApi = new PartsApi(client);
        PartRevisionDTO pr;
        PartIterationDTO pi;
        if (pRevision != null) {
            pr = partsApi.getPartRevision(workspace, pPartNumber, pRevision);
            pi = pIteration  > 0 ? pr.getPartIterations().get(pIteration-1) : LastIterationHelper.getLastIteration(pr);
        } else if (baselineId != null) {
            pi = partsApi.filterPartMasterInBaseline(workspace, pPartNumber, baselineId);
            pr = partsApi.getPartRevision(workspace, pPartNumber, pi.getVersion());
        } else {
            pr = partsApi.getLatestPartRevision(workspace, pPartNumber);
            pi = LastIterationHelper.getLastIteration(pr);
        }
        BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();

        if (nativeCADFile != null) {
            FileHelper fh = new FileHelper(client, output, langHelper);
            File result = fh.downloadPartFile(path, workspace, pPartNumber, pRevision, pi.getIteration(), nativeCADFile.getName(), pr.getType(), "nativecad", force);
            output.printInfo(result.getAbsolutePath());
        } else {
            output.printInfo(langHelper.getLocalizedMessage("NoFileForPart") + " : " + pPartNumber +
                    " " + pr.getVersion() + "." + pi.getIteration() + " (" + workspace + ")");
        }
        alreadyProcessed.add(pPartNumber);
        if (recursive) {
            for (PartUsageLinkDTO link : pi.getComponents()) {
                String linkNumber = link.getComponent().getNumber();
                getPart(linkNumber, null, null, alreadyProcessed);
            }
        }

    }
}
