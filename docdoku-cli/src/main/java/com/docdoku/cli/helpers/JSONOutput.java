/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.cli.helpers;

import com.docdoku.api.models.*;
import com.docdoku.api.models.utils.LastIterationHelper;
import org.kohsuke.args4j.CmdLineParser;

import javax.json.*;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class JSONOutput extends CliOutput {

    private static final Logger LOGGER = Logger.getLogger(JSONOutput.class.getName());
    private final static PrintStream ERROR_STREAM = System.err;
    private final static PrintStream OUTPUT_STREAM = System.out;

    public JSONOutput() {
    }

    @Override
    public void printException(Exception e) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("error", e.getMessage())
                .build();
        ERROR_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printCommandUsage(CommandLine cl) throws IOException {
        CmdLineParser parser = new CmdLineParser(cl);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        parser.printUsage(o);
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("description", cl.getDescription())
                .add("usage", o.toString())
                .build();
        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printUsage() {
        ERROR_STREAM.println("{\"usage\":\"Not available for json output\"}");
    }

    @Override
    public void printInfo(String s) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("info", s)
                .build();
        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printWorkspaces(List<WorkspaceDTO> workspaceDTOs) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for(WorkspaceDTO workspaceDTO:workspaceDTOs){
            jsonArray.add(workspaceDTO.getId());
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public void printPartRevisionsCount(int partMastersCount) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("count", partMastersCount)
                .build();
        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printPartRevisions(List<PartRevisionDTO> partRevisions) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (PartRevisionDTO partRevision : partRevisions) {
            jsonArrayBuilder.add(getPartRevision(partRevision, 0L));
        }
        OUTPUT_STREAM.println(jsonArrayBuilder.build().toString());
    }

    @Override
    public void printBaselines(List<ProductBaselineDTO> productBaselines) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (ProductBaselineDTO productBaseline : productBaselines) {
            JsonObject jsonBaseline = Json.createObjectBuilder()
                .add("id", productBaseline.hashCode())
                .add("name", productBaseline.getName())
                .add("configurationItem", productBaseline.getConfigurationItemId())
                .build();
            jsonArray.add(jsonBaseline);
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public void printPartRevision(PartRevisionDTO pr, long lastModified) {
        OUTPUT_STREAM.println(getPartRevision(pr, lastModified));
    }

    @Override
    public void printPartMaster(PartMaster pm, long lastModified) {
        // TODO : rewrite with DTO
        /*
        JsonArrayBuilder jsonRevisions = Json.createArrayBuilder();
        for (PartRevision pr : pm.getPartRevisions())
            jsonRevisions.add(getPartRevision(pr, lastModified));

        JsonObject jsonMaster = Json.createObjectBuilder()
                .add("revisions", jsonRevisions.build()).build();

        //Add jsonMaster ?
        OUTPUT_STREAM.println(getPartRevision(pm.getLastRevision(), lastModified));
        */
    }

    @Override
    public void printConversion(ConversionDTO conversion) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("pending", conversion.getPending())
                .add("succeed", conversion.getSucceed())
                .add("startDate", conversion.getStartDate().toString())
                .add("endDate", conversion.getEndDate().toString())
                .build();
        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printAccount(AccountDTO accountDTO) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("login", accountDTO.getLogin())
                .add("language", accountDTO.getLanguage())
                .add("email", accountDTO.getEmail())
                .add("timezone", accountDTO.getTimeZone())
                .build();

        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printDocumentRevision(DocumentRevisionDTO documentRevisionDTO, long lastModified) {
        OUTPUT_STREAM.println(getDocumentRevision(documentRevisionDTO, lastModified));
    }

    @Override
    public void printDocumentRevisions(List<DocumentRevisionDTO> documentRevisions) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (DocumentRevisionDTO documentRevision : documentRevisions) {
            jsonArray.add(getDocumentRevision(documentRevision, 0L));
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public void printFolders(List<FolderDTO> folders) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (FolderDTO folder : folders) {
            jsonArray.add(folder.getName());
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public FilterInputStream getMonitor(long maximum, InputStream in) {
        return new JSONProgressMonitorInputStream(maximum, in);
    }

    private JsonObject getPartRevision(PartRevisionDTO pr, long lastModified) {

        JsonObjectBuilder jsonStatusBuilder = Json.createObjectBuilder();

        if (pr != null) {
            UserDTO user = pr.getCheckOutUser();
            String login = user != null ? user.getLogin() : "";
            Date checkoutDate = pr.getCheckOutDate();
            Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
            jsonStatusBuilder.add("isReleased", pr.getStatus().equals(PartRevisionDTO.StatusEnum.RELEASED));
            jsonStatusBuilder.add("isCheckedOut", user != null);
            jsonStatusBuilder.add("partNumber", pr.getNumber());
            jsonStatusBuilder.add("checkoutUser", login);
            if(timeStamp != null) {
                jsonStatusBuilder.add("checkoutDate", timeStamp);
            }else{
                jsonStatusBuilder.add("checkoutDate", JsonValue.NULL);
            }
            jsonStatusBuilder.add("workspace", pr.getWorkspaceId());
            jsonStatusBuilder.add("version", pr.getVersion());

            if(pr.getDescription() != null) {
                jsonStatusBuilder.add("description", pr.getDescription());
            }else{
                jsonStatusBuilder.add("description", JsonValue.NULL);
            }

            jsonStatusBuilder.add("lastModified", lastModified);

            PartIterationDTO lastIteration = LastIterationHelper.getLastIteration(pr);

            if (lastIteration != null){

            }
            if(lastIteration.getNativeCADFile() != null) {
                BinaryResourceDTO nativeCADFile = lastIteration.getNativeCADFile();
                if(nativeCADFile != null){
                    jsonStatusBuilder.add("cadFileName", nativeCADFile.getFullName());
                }
            }

            List<PartIterationDTO> partIterations = pr.getPartIterations();

            if (partIterations != null) {
                JsonArrayBuilder jsonIterationsBuilder = Json.createArrayBuilder();
                for (PartIterationDTO partIteration : partIterations) {
                    jsonIterationsBuilder.add(partIteration.getIteration());
                }
                jsonStatusBuilder.add("iterations", jsonIterationsBuilder.build());
            }

        }

        return jsonStatusBuilder.build();
    }

    private JsonObject getDocumentRevision(DocumentRevisionDTO documentRevisionDTO, long lastModified) {

        JsonObjectBuilder jsonStatusBuilder = Json.createObjectBuilder();

        if (documentRevisionDTO != null) {
            UserDTO userDTO = documentRevisionDTO.getCheckOutUser();
            String login = userDTO != null ? userDTO.getLogin() : "";
            Date checkoutDate = documentRevisionDTO.getCheckOutDate();
            Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
            jsonStatusBuilder.add("isCheckedOut", checkoutDate != null );
            jsonStatusBuilder.add("id", documentRevisionDTO.getDocumentMasterId());
            jsonStatusBuilder.add("checkoutUser", login);
            if(timeStamp != null) {
                jsonStatusBuilder.add("checkoutDate", timeStamp);
            }else{
                jsonStatusBuilder.add("checkoutDate", JsonValue.NULL);
            }
            jsonStatusBuilder.add("workspace", documentRevisionDTO.getWorkspaceId());
            jsonStatusBuilder.add("version", documentRevisionDTO.getVersion());

            if(documentRevisionDTO.getDescription() != null) {
                jsonStatusBuilder.add("description", documentRevisionDTO.getDescription());
            }else{
                jsonStatusBuilder.add("description", JsonValue.NULL);
            }

            jsonStatusBuilder.add("lastModified", lastModified);

            DocumentIterationDTO lastIterationDTO = getDocumentRevisionDTOLastIteration(documentRevisionDTO);

            if (lastIterationDTO != null && lastIterationDTO.getAttachedFiles() != null) {
                JsonArrayBuilder jsonFilesBuilder = Json.createArrayBuilder();
                for(BinaryResourceDTO binaryResourceDTO:lastIterationDTO.getAttachedFiles())
                    jsonFilesBuilder.add(binaryResourceDTO.getFullName());

                jsonStatusBuilder.add("files", jsonFilesBuilder.build());
            }

            List<DocumentIterationDTO> documentIterations = documentRevisionDTO.getDocumentIterations();
            if (documentIterations != null) {
                JsonArrayBuilder jsonIterationsBuilder = Json.createArrayBuilder();
                for (DocumentIterationDTO documentIteration : documentIterations) {
                    jsonIterationsBuilder.add(documentIteration.getIteration());
                }
                jsonStatusBuilder.add("iterations", jsonIterationsBuilder.build());
            }
        }
        return jsonStatusBuilder.build();
    }

    private DocumentIterationDTO getDocumentRevisionDTOLastIteration(DocumentRevisionDTO documentRevisionDTO) {
        int iterations = documentRevisionDTO.getDocumentIterations().size();
        return documentRevisionDTO.getDocumentIterations().get(iterations-1);
    }
}
