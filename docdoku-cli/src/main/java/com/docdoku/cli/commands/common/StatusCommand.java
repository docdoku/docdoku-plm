package com.docdoku.cli.commands.common;

import com.docdoku.api.client.ApiException;
import com.docdoku.api.models.DocumentRevisionDTO;
import com.docdoku.api.models.PartRevisionDTO;
import com.docdoku.api.services.DocumentApi;
import com.docdoku.api.services.PartApi;
import com.docdoku.api.services.PartsApi;
import com.docdoku.cli.commands.BaseCommandLine;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;

/**
 * @author Jean-Luc Mounsamy
 */
public class StatusCommand extends BaseCommandLine {

    @Option(
            name = "-doc",
            aliases = "--document",
            usage = "Use to specify target as document",
            forbids = {"-get"}
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
            usage = "workspace on which operations occur",
            metaVar = "<workspace>"
    )
    protected String workspace;

    @Option(
            name = "-o",
            aliases = "--id",
            usage = "the id of the target to get a status; if not specified choose the target corresponding to the file",
            metaVar = "<id>"
    )
    private String id;

    @Option(
            name = "-r",
            aliases = "--revision",
            usage = "specify revision of the target to get a status ('A', 'B'...); if not specified the target " +
                    "identity (id and revision) corresponding to the file will be selected",
            metaVar = "<revision>"
    )
    private String revision;

    @Argument(
            metaVar = "[<file>]",
            index = 0,
            usage = "specify the file of the target to get a status"
    )
    private File file;

    private long lastModified;

    @Override
    public void execImpl() throws Exception {
        try {
            if(!document && !part && file == null) {
                throw new IllegalArgumentException(langHelper.getLocalizedMessage("StatusCommandNoFileSupplied"));
            }
            if(document) {
                processDocument();
            } else if (part) {
                processPart();
            } else {
                processFile();
            }
        } catch (Exception e) {
            output.printException(e);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("StatusCommandDescription");
    }

    private void processDocument() throws IOException {
        if (id == null || revision == null || workspace == null) {
            loadMetadata();
        }
        try {
            DocumentApi documentApi = new DocumentApi(client);
            DocumentRevisionDTO documentRevisionDTO = documentApi.getDocumentRevision(workspace, id, revision);
            output.printDocumentRevision(documentRevisionDTO, lastModified);
        } catch (ApiException e) {
            MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
            meta.deleteEntryInfo(file.getAbsolutePath());
            output.printException(e);
        }
    }

    private void processPart() throws IOException {
        if(workspace == null || id == null || revision == null) {
            loadMetadata();
        }
        try {
            PartsApi partsApi = new PartsApi(client);
            PartRevisionDTO partRevision;
            if (revision == null) {
                partRevision = partsApi.getLatestPartRevision(workspace, id);
            } else {
                partRevision = partsApi.getPartRevision(workspace, id, revision);
            }
            output.printPartRevision(partRevision, lastModified);
        } catch (ApiException e) {
            MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
            meta.deleteEntryInfo(file.getAbsolutePath());
            output.printException(e);
        }
    }

    private void processFile() throws IOException {
        if(file == null)
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("StatusCommandMissingFile"));
        MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
        String filePath = file.getAbsolutePath();

        String metaWorkspace = meta.getWorkspace(filePath);
        long metaLastModified = meta.getLastModifiedDate(filePath);
        String strRevision = meta.getRevision(filePath);

        if(meta.isDocumentRelated(filePath)){
            String ref = meta.getDocumentId(filePath);
            try {
                DocumentApi documentApi = new DocumentApi(client);
                DocumentRevisionDTO documentRevision = documentApi.getDocumentRevision(metaWorkspace,ref,strRevision);
                output.printDocumentRevision(documentRevision,metaLastModified);
            } catch (ApiException e) {
                meta.deleteEntryInfo(file.getAbsolutePath());
                output.printException(e);
            }
        }
        else if(meta.isPartRelated(filePath)){
            String ref = meta.getPartNumber(filePath);
            try {
                PartApi partApi = new PartApi(client);
                PartRevisionDTO partRevision = partApi.getPartRevision(metaWorkspace, ref, strRevision);
                output.printPartRevision(partRevision, metaLastModified);
            } catch (ApiException e) {
                meta.deleteEntryInfo(file.getAbsolutePath());
                output.printException(e);
            }
        }else{
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("FileNotIndexedException"));
        }
    }

    private void loadMetadata() throws IOException {
        if (file == null)
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("StatusCommandNoFileSuppliedToGetMissingArgs"));
        MetaDirectoryManager meta = new MetaDirectoryManager(file.getParentFile());
        String filePath = file.getAbsolutePath();
        if(document)
            id = meta.getDocumentId(filePath);
        else if(part)
            id = meta.getPartNumber(filePath);
        workspace = meta.getWorkspace(filePath);
        lastModified = meta.getLastModifiedDate(filePath);
        String strRevision = meta.getRevision(filePath);
        if (id == null || strRevision == null || workspace == null) {
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("StatusCommandCantGetMissingArgsFromFile"));
        }
        revision = strRevision;
    }
}
