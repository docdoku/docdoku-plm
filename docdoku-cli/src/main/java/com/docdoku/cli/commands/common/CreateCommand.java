package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import com.docdoku.api.services.FoldersApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.FileHelper;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 * @author Jean-Luc Mounsamy
 */
public class CreateCommand extends BaseCommandLine {
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
            required = true,
            usage = "the id of the target to save"
    )
    private String id;

    @Option(
            metaVar = "<description>",
            name = "-d",
            aliases = "--description",
            usage = "the description of the document to save"
    )
    private String description;

    @Option(
            metaVar = "<name>",
            name = "-N", aliases = "--name",
            usage = "the name of the target to save"
    )
    private String name;

    @Option(
            name = "-s",
            aliases = "--standard",
            metaVar = "<format>",
            usage = "save as standard part",
            forbids = {"-doc"}
    )
    protected boolean standardPart = false;

    @Argument(
            metaVar = "<file>",
            required = true,
            usage = "specify the file of the document to import")
    private File file;

    @Override
    public void execImpl() throws Exception {
        try {
            if(!document && !part) {
                throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentOrPartOptionMandatory"));
            }
            if(document) {
                processDocument();
            } else {
                processPart();
            }
        } catch (Exception e) {
            output.printException(e);
            output.printCommandUsage(this);
        }
    }

    private void processPart() {
        try {
            PartsApi partsApi = new PartsApi(client);
            PartCreationDTO partCreationDTO = new PartCreationDTO();
            partCreationDTO.setDescription(description);
            partCreationDTO.setWorkspaceId(workspace);
            partCreationDTO.setName(name);
            partCreationDTO.setNumber(id);
            partCreationDTO.setStandardPart(standardPart);
            PartRevisionDTO pr = partsApi.createNewPart(workspace, partCreationDTO);

            PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(pr);
            PartRevisionDTO partRPK = new PartRevisionDTO();
            partRPK.setWorkspaceId(workspace);
            partRPK.setNumber(id);
            partRPK.setVersion(pr.getVersion());

            PartIterationDTO partIPK = new PartIterationDTO();
            partIPK.setWorkspaceId(workspace);
            partIPK.setNumber(id);
            partIPK.setVersion(pr.getVersion());
            partIPK.setIteration(lastIteration.getIteration());

            FileHelper fh = new FileHelper(client, output, langHelper);

            fh.uploadPartFile(workspace, id, "A", 1, file);
            output.printInfo(langHelper.getLocalizedMessage("PartCreationSuccess"));
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("PartCreationFailed"));
            output.printException(e);
        }
    }

    private void processDocument() {
        try {
            FoldersApi foldersApi = new FoldersApi(client);
            DocumentCreationDTO documentCreationDTO = new DocumentCreationDTO();
            documentCreationDTO.setTitle(name);
            documentCreationDTO.setWorkspaceId(workspace);
            documentCreationDTO.setDescription(description);
            documentCreationDTO.setReference(id);

            foldersApi.createDocumentMasterInFolder(workspace, documentCreationDTO, workspace);
            DocumentRevisionDTO docRPK = new DocumentRevisionDTO();
            docRPK.setWorkspaceId(workspace);
            docRPK.setDocumentMasterId(id);
            docRPK.setVersion("A");

            DocumentIterationDTO docIPK = new DocumentIterationDTO();
            docIPK.setWorkspaceId(workspace);
            docIPK.setDocumentMasterId(id);
            docIPK.setVersion("A");
            docIPK.setIteration(1);

            FileHelper fh = new FileHelper(client, output, langHelper);
            fh.uploadDocumentFile(workspace, id, "A", 1, file);
            output.printInfo(langHelper.getLocalizedMessage("DocumentCreationSuccess"));
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("DocumentCreationFailed"));
            output.printException(e);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("CreateCommandDescription");
    }
}
