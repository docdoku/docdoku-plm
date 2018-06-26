package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.services.DocumentsApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.List;

/**
 * @author Jean-Luc Mounsamy
 */
public class SearchCommand extends BaseCommandLine {
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
            name = "-s",
            aliases = "--search",
            required = true,
            metaVar = "<search>",
            usage = "search string"
    )
    protected String searchValue;

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
        List<PartRevisionDTO> partRevisions = partsApi.searchPartRevisions(workspace, searchValue, null, null,
                null, null, null, null, null, null, null, null, null, null, 0 ,10000, false);
        output.printPartRevisions(partRevisions);
    }

    private void processDocument() throws ApiException {
        DocumentsApi documentsApi = new DocumentsApi(client);
        List<DocumentRevisionDTO> documentRevisions = documentsApi.searchDocumentRevision(workspace, searchValue,
                null, null, null, null, null, null, null, null, null, null, null, null, null, 0, 10000, false);
        output.printDocumentRevisions(documentRevisions);
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("SearchCommandDescription");
    }
}
