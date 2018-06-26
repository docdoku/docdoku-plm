package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.CountDTO;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.services.DocumentsApi;
import com.docdoku.api.services.FoldersApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 * @author Jean-Luc Mounsamy
 */
public class ListCommand extends BaseCommandLine {
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
            name = "-f",
            aliases = "--folder",
            usage = "remote folder to list, default is workspace root folder",
            forbids = {"-part"}
    )
    private String folder = null;

    @Option(
            name = "-co",
            aliases = "--checkedOut",
            usage = "list only checked out files",
            forbids = {"-part"}
    )
    private boolean checkedOutDocsOnly = false;

    @Option(
            name="-c",
            aliases = "--count",
            usage="return the number of part revisions within the workspace",
            forbids = {"-doc"}
    )
    private boolean count;

    @Option(
            name="-s",
            aliases = "--start",
            usage="start offset",
            forbids = {"-doc"}
    )
    private int start;

    @Option(
            name="-m",
            aliases = "--max-results",
            usage="max results",
            forbids = {"-doc"}
    )
    private int max;

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

    private void processPart() throws ApiException {
        PartsApi partsApi = new PartsApi(client);
        if(count) {
            CountDTO countDTO = partsApi.getTotalNumberOfParts(workspace);
            output.printPartRevisionsCount(countDTO.getCount());
        } else {
            List<PartRevisionDTO> partRevisions = partsApi.getPartRevisions(workspace, start, max);
            output.printPartRevisions(partRevisions);
        }
    }

    private void processDocument() throws ApiException {
        if (checkedOutDocsOnly) {
            DocumentsApi documentsApi = new DocumentsApi(client);
            List<DocumentRevisionDTO> documentRevisions = documentsApi.getCheckedOutDocuments(workspace);
            output.printDocumentRevisions(documentRevisions);
        } else {
            FoldersApi foldersApi = new FoldersApi(client);
            String decodedPath = folder == null ? workspace : workspace + "/" + folder;
            List<DocumentRevisionDTO> documentRevisions = foldersApi.getDocumentsWithGivenFolderIdAndWorkspaceId(workspace, decodedPath);
            output.printDocumentRevisions(documentRevisions);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("ListCommandDescription");
    }
}
