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

import com.docdoku.core.common.Account;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import org.kohsuke.args4j.CmdLineParser;

import javax.json.*;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
        ERROR_STREAM.println("{\"usage\":\"TODO\"}");
    }

    @Override
    public void printInfo(String s) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("info", s)
                .build();
        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printWorkspaces(Workspace[] workspaces) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (int i = 0; i < workspaces.length; i++) {
            jsonArray.add(workspaces[i].getId());
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
    public void printPartRevisions(List<PartRevision> partRevisions) {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (PartRevision partRevision : partRevisions) {
            jsonArrayBuilder.add(getPartRevision(partRevision, 0L));
        }
        OUTPUT_STREAM.println(jsonArrayBuilder.build().toString());
    }

    @Override
    public void printBaselines(List<ProductBaseline> productBaselines) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (ProductBaseline productBaseline : productBaselines) {
            JsonObject jsonBaseline = Json.createObjectBuilder()
                .add("id", productBaseline.hashCode())
                .add("name", productBaseline.getName())
                .add("configurationItem", productBaseline.getConfigurationItem().getId())
                .build();
            jsonArray.add(jsonBaseline);
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public void printPartRevision(PartRevision pr, long lastModified) {
        OUTPUT_STREAM.println(getPartRevision(pr, lastModified));
    }

    @Override
    public void printPartMaster(PartMaster pm, long lastModified) {
        JsonArrayBuilder jsonRevisions = Json.createArrayBuilder();
        for (PartRevision pr : pm.getPartRevisions())
            jsonRevisions.add(getPartRevision(pr, lastModified));

        JsonObject jsonMaster = Json.createObjectBuilder()
                .add("revisions", jsonRevisions.build()).build();

        //Add jsonMaster ?
        OUTPUT_STREAM.println(getPartRevision(pm.getLastRevision(), lastModified));
    }

    @Override
    public void printConversion(Conversion conversion) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("pending", conversion.isPending())
                .add("succeed", conversion.isSucceed())
                .add("startDate", conversion.getStartDate().toString())
                .add("endDate", conversion.getEndDate().toString())
                .build();
        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printAccount(Account account) {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add("login", account.getLogin())
                .add("language", account.getLanguage())
                .add("email", account.getEmail())
                .add("timezone", account.getTimeZone())
                .build();

        OUTPUT_STREAM.println(jsonObj.toString());
    }

    @Override
    public void printDocumentRevision(DocumentRevision documentRevision, long lastModified) {
        OUTPUT_STREAM.println(getDocumentRevision(documentRevision, lastModified));
    }

    @Override
    public void printDocumentRevisions(DocumentRevision[] documentRevisions) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (DocumentRevision documentRevision : documentRevisions) {
            jsonArray.add(getDocumentRevision(documentRevision, 0L));
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public void printFolders(String[] folders) {
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();
        for (String folder : folders) {
            jsonArray.add(folder);
        }
        OUTPUT_STREAM.println(jsonArray.build().toString());
    }

    @Override
    public FilterInputStream getMonitor(long maximum, InputStream in) {
        return new JSONProgressMonitorInputStream(maximum, in);
    }

    private JsonObject getPartRevision(PartRevision pr, long lastModified) {

        JsonObjectBuilder jsonStatusBuilder = Json.createObjectBuilder();

        if (pr != null) {
            User user = pr.getCheckOutUser();
            String login = user != null ? user.getLogin() : "";
            Date checkoutDate = pr.getCheckOutDate();
            Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
            jsonStatusBuilder.add("isReleased", pr.isReleased());
            jsonStatusBuilder.add("isCheckedOut", pr.isCheckedOut());
            jsonStatusBuilder.add("partNumber", pr.getPartMasterNumber());
            jsonStatusBuilder.add("checkoutUser", login);
            jsonStatusBuilder.add("checkoutDate", timeStamp);
            jsonStatusBuilder.add("workspace", pr.getPartMasterWorkspaceId());
            jsonStatusBuilder.add("version", pr.getVersion());
            jsonStatusBuilder.add("description", pr.getDescription());
            jsonStatusBuilder.add("lastModified", lastModified);

            if (pr.getLastIteration() != null && pr.getLastIteration().getNativeCADFile() != null) {
                String nativeCADFileName = pr.getLastIteration().getNativeCADFile().getName();
                jsonStatusBuilder.add("cadFileName", nativeCADFileName);
            }

            List<PartIteration> partIterations = pr.getPartIterations();
            if (partIterations != null) {
                JsonArrayBuilder jsonIterationsBuilder = Json.createArrayBuilder();
                for (PartIteration partIteration : partIterations) {
                    jsonIterationsBuilder.add(partIteration.getIteration());
                }
                jsonStatusBuilder.add("iterations", jsonIterationsBuilder.build());
            }

        }

        return jsonStatusBuilder.build();
    }

    private JsonObject getDocumentRevision(DocumentRevision dr, long lastModified) {

        JsonObjectBuilder jsonStatusBuilder = Json.createObjectBuilder();

        if (dr != null) {
            User user = dr.getCheckOutUser();
            String login = user != null ? user.getLogin() : "";
            Date checkoutDate = dr.getCheckOutDate();
            Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
            jsonStatusBuilder.add("isCheckedOut", dr.isCheckedOut());
            jsonStatusBuilder.add("id", dr.getDocumentMasterId());
            jsonStatusBuilder.add("checkoutUser", login);
            jsonStatusBuilder.add("checkoutDate", timeStamp);
            jsonStatusBuilder.add("workspace", dr.getDocumentMasterWorkspaceId());
            jsonStatusBuilder.add("version", dr.getVersion());
            jsonStatusBuilder.add("description", dr.getDescription());
            jsonStatusBuilder.add("lastModified", lastModified);

            if (dr.getLastIteration() != null && dr.getLastIteration().getAttachedFiles() != null) {
                JsonArrayBuilder jsonFilesBuilder = Json.createArrayBuilder();
                for(BinaryResource bin:dr.getLastIteration().getAttachedFiles())
                    jsonFilesBuilder.add(bin.toString());

                jsonStatusBuilder.add("files", jsonFilesBuilder.build());
            }

            List<DocumentIteration> documentIterations = dr.getDocumentIterations();
            if (documentIterations != null) {
                JsonArrayBuilder jsonIterationsBuilder = Json.createArrayBuilder();
                for (DocumentIteration documentIteration : documentIterations) {
                    jsonIterationsBuilder.add(documentIteration.getIteration());
                }
                jsonStatusBuilder.add("iterations", jsonIterationsBuilder.build());
            }
        }
        return jsonStatusBuilder.build();
    }
}
