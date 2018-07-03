package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.DocumentIterationDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.PartIterationDTO;
import com.docdoku.api.models.PartRevisionDTO;
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
public class PutCommand extends BaseCommandLine {
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

    @Option(name = "-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage = "workspace on which operations occur")
    protected String workspace;

    @Option(metaVar = "<id>", name = "-o", aliases = "--id", usage = "the id of the target to save; if not specified choose the target corresponding to the file if it has already been imported")
    private String id;

    @Option(metaVar = "<revision>", name = "-r", aliases = "--revision", usage = "specify revision of the target to save ('A', 'B'...); if not specified the target identity (id and revision) corresponding to the file will be selected")
    private String revision;

    @Argument(metaVar = "<file>", required = true, index = 0, usage = "specify the file of the target to import")
    private File file;

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
        return langHelper.getLocalizedMessage("PutCommandDescription");
    }

    private void loadMetadata() throws IOException {
        MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
        String filePath = file.getAbsolutePath();
        if(document) {
            id = meta.getDocumentId(filePath);
        } else if(part) {
            id = meta.getPartNumber(filePath);
        }
        String strRevision = meta.getRevision(filePath);
        if (id == null || strRevision == null) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2"));
        }
        revision = strRevision;
    }

    private void processDocument() throws ApiException {
        DocumentRevisionDTO docRPK = new DocumentRevisionDTO();
        docRPK.setWorkspaceId(workspace);
        docRPK.setDocumentMasterId(id);
        docRPK.setVersion(revision);

        DocumentApi documentApi = new DocumentApi(client);
        DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace, id, revision);
        DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);

        DocumentIterationDTO docIPK = new DocumentIterationDTO();
        docIPK.setWorkspaceId(workspace);
        docIPK.setDocumentMasterId(id);
        docIPK.setVersion(revision);
        docIPK.setIteration(di.getIteration());

        FileHelper fh = new FileHelper(client, output, langHelper);
        fh.uploadDocumentFile(workspace, id, revision, di.getIteration(), file);
    }

    private void processPart() throws ApiException {
        PartRevisionDTO partRPK = new PartRevisionDTO();
        partRPK.setWorkspaceId(workspace);
        partRPK.setNumber(id);
        partRPK.setVersion(revision);

        PartApi partApi = new PartApi(client);
        PartRevisionDTO pr = partApi.getPartRevision(workspace, id, revision);
        PartIterationDTO pi = LastIterationHelper.getLastIteration(pr);

        PartIterationDTO partIPK = new PartIterationDTO();
        partIPK.setWorkspaceId(workspace);
        partIPK.setNumber(id);
        partIPK.setVersion(revision);
        partIPK.setIteration(pi.getIteration());

        FileHelper fh = new FileHelper(client, output, langHelper);
        fh.uploadPartFile(workspace, id, revision, pi.getIteration(), file);
    }
}
