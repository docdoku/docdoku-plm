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

public class CheckInCommand extends BaseCommandLine {
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
            usage = "the id of the target to check in; if not specified choose the target corresponding to the file"
    )
    private String id;

    @Option(
            metaVar = "<revision>",
            name = "-r",
            aliases = "--revision",
            usage = "specify revision of the target to check in ('A', 'B'...); if not specified the target " +
                    "identity (id and revision) corresponding to the file will be selected"
    )
    private String revision;

    @Argument(
            metaVar = "[<file> | <dir>]",
            index = 0,
            usage = "specify the file of the target to check in or the path where files are stored (default is working directory)"
    )
    private File path = new File(System.getProperty("user.dir"));

    @Option(
            name = "-n",
            aliases = "--no-upload",
            usage = "do not upload the file of the target if any"
    )
    private boolean noUpload;

    @Option(
            metaVar = "<message>",
            name = "-m",
            aliases = "--message",
            usage = "a message specifying the iteration modifications"
    )
    private String message;

    @Override
    public void execImpl() throws Exception {
        try {
            if(!document && !part)
                throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentOrPartOptionMandatory"));
            if (id == null || revision == null)
                loadMetadata();
            if(document)
                processDocument();
            else
                processPart();
        } catch (Exception e) {
            output.printException(e);
            output.printCommandUsage(this);
        }
    }

    private void loadMetadata() throws IOException {
        if(path.isDirectory()){
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified1"));
        }
        MetaDirectoryManager meta = new MetaDirectoryManager(path.getParentFile());
        String filePath = path.getAbsolutePath();
        if(document)
            id = meta.getDocumentId(filePath);
        else if(part)
            id = meta.getPartNumber(filePath);
        String strRevision = meta.getRevision(filePath);
        if(id==null || strRevision==null){
            throw new IllegalArgumentException(langHelper.getLocalizedMessage("DocumentIdOrRevisionNotSpecified2"));
        }
        revision = strRevision;
        path=path.getParentFile();
    }

    private void processPart() {
        try {
            PartApi partApi = new PartApi(client);

            PartRevisionDTO pr = partApi.getPartRevision(workspace, id, revision);
            PartIterationDTO pi = LastIterationHelper.getLastIteration(pr);

            PartRevisionDTO partRPK = new PartRevisionDTO();
            partRPK.setWorkspaceId(workspace);
            partRPK.setNumber(id);
            partRPK.setVersion(revision);

            PartIterationDTO partIPK = new PartIterationDTO();
            partIPK.setWorkspaceId(workspace);
            partIPK.setNumber(id);
            partIPK.setVersion(revision);
            partIPK.setIteration(pi.getIteration());

            if (!noUpload) {
                BinaryResourceDTO nativeCADFile = pi.getNativeCADFile();
                if (nativeCADFile != null) {

                    String fileName = nativeCADFile.getName();
                    File localFile = new File(path, fileName);
                    if (localFile.exists()) {
                        FileHelper fh = new FileHelper(client, output, langHelper);
                        fh.uploadPartFile(workspace, id, revision, pi.getIteration(), path);
                    }
                }
            }

            if (message != null && !message.isEmpty()) {
                pi.setIterationNote(message);
                partApi.updatePartIteration(workspace, id, revision, pi.getIteration(), pi);
            }

            partApi.checkIn(workspace, id, revision);

            output.printInfo(langHelper.getLocalizedMessage("PartCheckInSuccess"));
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("PartCheckInFailed"));
            output.printException(e);
        }
    }

    private void processDocument() {
        try {
            DocumentApi documentApi = new DocumentApi(client);
            DocumentRevisionDTO dr = documentApi.getDocumentRevision(workspace, id, revision);

            DocumentIterationDTO di = LastIterationHelper.getLastIteration(dr);
            DocumentIterationDTO docIPK = new DocumentIterationDTO();
            docIPK.setWorkspaceId(workspace);
            docIPK.setDocumentMasterId(id);
            docIPK.setVersion(revision);
            docIPK.setIteration(di.getIteration());

            if (!noUpload && !di.getAttachedFiles().isEmpty()) {

                for (BinaryResourceDTO binaryResourceDTO : di.getAttachedFiles()) {
                    String fileName = binaryResourceDTO.getName();
                    File localFile = new File(path, fileName);
                    if (localFile.exists()) {
                        FileHelper fh = new FileHelper(client, output, langHelper);

                        fh.uploadDocumentFile(workspace, id, revision, di.getIteration(), path);
                    }
                }
            }

            if (message != null && !message.isEmpty()) {
                di.setRevisionNote(message);
                documentApi.updateDocumentIteration(workspace, id, revision, di.getIteration(), di);
            }

            documentApi.checkInDocument(workspace, id, revision);

            output.printInfo(langHelper.getLocalizedMessage("DocumentCheckInSuccess"));
        } catch (ApiException e) {
            output.printInfo(langHelper.getLocalizedMessage("DocumentCheckInFailed"));
            output.printException(e);
        }
    }

    @Override
    public String getDescription() throws IOException {
        return langHelper.getLocalizedMessage("CheckInCommandDescription");
    }
}
