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

import com.docdoku.cli.interfaces.CommandLine;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kohsuke.args4j.CmdLineParser;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JSONOutput  extends CliOutput {

    private Locale locale;

    public JSONOutput(Locale pLocale) {
        locale = pLocale;
    }

    @Override
    public void printException(Exception e) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("error", e.getMessage());
        } catch (JSONException e1) {

        }
        System.err.println(jsonObj.toString());
    }

    @Override
    public void printCommandUsage(CommandLine cl) throws IOException {
        CmdLineParser parser = new CmdLineParser(cl);
        JSONObject jsonObj = new JSONObject();
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        parser.printUsage(o);
        try {
            jsonObj.put("description", cl.getDescription());
            jsonObj.put("usage", o.toString());
        } catch (JSONException e) {

        }
        System.out.println(jsonObj.toString());
    }

    @Override
    public void printUsage() {
        System.err.println("{\"usage\":\"TODO\"}");
    }

    @Override
    public void printInfo(String s) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("info", s);
        } catch (JSONException e1) {

        }
        System.out.println(jsonObj.toString());
    }

    @Override
    public void printWorkspaces(Workspace[] workspaces) {
        JSONArray wks = new JSONArray();
        for(int i = 0 ; i < workspaces.length; i++){
            try {
                wks.put(i,workspaces[i].getId());
            } catch (JSONException e) {

            }
        }
        System.out.println(wks.toString());
    }

    @Override
    public void printPartRevisionsCount(int partMastersCount) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("count",partMastersCount);
        } catch (JSONException e) {

        }
        System.out.println(jsonObject.toString());
    }

    @Override
    public void printPartRevisions(List<PartRevision> partRevisions) {
        JSONArray jsonArray = new JSONArray();
        for (PartRevision partRevision : partRevisions) {
            jsonArray.put(getPartRevision(partRevision, 0L));
        }
        System.out.println(jsonArray.toString());
    }

    @Override
    public void printBaselines(List<ProductBaseline> productBaselines) {
        JSONArray jsonArray = new JSONArray();
        for(ProductBaseline productBaseline : productBaselines) {
            try {
                JSONObject baselineObject = new JSONObject();
                baselineObject.put("id", productBaseline.hashCode());
                baselineObject.put("name", productBaseline.getName());
                baselineObject.put("configurationItem", productBaseline.getConfigurationItem().getId());
                jsonArray.put(baselineObject);
            } catch (JSONException e) {

            }
        }
        System.out.println(jsonArray.toString());
    }

    @Override
    public void printPartRevision(PartRevision pr, long lastModified) {
        System.out.println(getPartRevision(pr, lastModified));
    }

    @Override
    public void printPartMaster(PartMaster pm, long lastModified) {
        JSONObject partMaster = new JSONObject();
        try {
            JSONArray revisions = new JSONArray();
            partMaster.put("revisions", revisions);
            for (PartRevision pr : pm.getPartRevisions()) {
                revisions.put(getPartRevision(pr, lastModified));
            }
        }catch(JSONException e){

        }

        System.out.println(getPartRevision(pm.getLastRevision(), lastModified));
    }

    @Override
    public void printConversion(Conversion conversion) throws JSONException {
        JSONObject cv = new JSONObject();
        cv.put("pending",conversion.isPending());
        cv.put("succeed",conversion.isSucceed());
        cv.put("startDate",conversion.getStartDate());
        cv.put("endDate",conversion.getEndDate());
        System.out.println(cv.toString());
    }

    @Override
    public void printAccount(Account account) throws JSONException {
        JSONObject cv = new JSONObject();
        cv.put("login",account.getLogin());
        cv.put("language",account.getLanguage());
        cv.put("email",account.getEmail());
        cv.put("timezone",account.getTimeZone());
        System.out.println(cv.toString());
    }

    @Override
    public void printDocumentRevision(DocumentRevision documentRevision, long lastModified) {
        System.out.println(getDocumentRevision(documentRevision, lastModified));
    }

    @Override
    public FilterInputStream getMonitor(long maximum, InputStream in) {
        return new JSONProgressMonitorInputStream(maximum,in);
    }

    private JSONObject getPartRevision(PartRevision pr, long lastModified) {

        JSONObject status = new JSONObject();

        if(pr != null){

            try{

                User user = pr.getCheckOutUser();
                String login = user != null ? user.getLogin() : "";
                Date checkoutDate = pr.getCheckOutDate();
                Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
                status.put("isReleased", pr.isReleased());
                status.put("isCheckedOut", pr.isCheckedOut());
                status.put("partNumber", pr.getPartMasterNumber());
                status.put("checkoutUser", login);
                status.put("checkoutDate", timeStamp);
                status.put("workspace", pr.getPartMasterWorkspaceId());
                status.put("version", pr.getVersion());
                status.put("description", pr.getDescription());
                status.put("lastModified", lastModified);

                if(pr.getLastIteration() != null && pr.getLastIteration().getNativeCADFile() != null) {
                    String nativeCADFileName  = pr.getLastIteration().getNativeCADFile().getName();
                    status.put("cadFileName", nativeCADFileName);
                }

                List<PartIteration> partIterations = pr.getPartIterations();
                JSONArray partIterationJSonArray;
                if (partIterations != null) {
                    partIterationJSonArray = new JSONArray();
                    for(PartIteration partIteration : partIterations) {
                        partIterationJSonArray.put(partIteration.getIteration());
                    }
                    status.put("iterations", partIterationJSonArray);
                }

            }catch (JSONException e){

            }
        }

        return status;
    }

    private JSONObject getDocumentRevision(DocumentRevision dr, long lastModified) {

        JSONObject status = new JSONObject();

        if(dr != null){

            try{

                User user = dr.getCheckOutUser();
                String login = user != null ? user.getLogin() : "";
                Date checkoutDate = dr.getCheckOutDate();
                Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
                status.put("isCheckedOut", dr.isCheckedOut());
                status.put("id", dr.getDocumentMasterKey().getId());
                status.put("checkoutUser", login);
                status.put("checkoutDate", timeStamp);
                status.put("workspace", dr.getWorkspaceId());
                status.put("version", dr.getVersion());
                status.put("description", dr.getDescription());
                status.put("lastModified", lastModified);

                if(dr.getLastIteration() != null && dr.getLastIteration().getAttachedFiles() != null) {
                    JSONArray files = new JSONArray(dr.getLastIteration().getAttachedFiles());
                    status.put("cadFileName", files);
                }

                List<DocumentIteration> documentIterations = dr.getDocumentIterations();
                if (documentIterations != null) {
                    JSONArray documentIterationsArray = new JSONArray(documentIterations);
                    status.put("iterations", documentIterationsArray);
                }

            }catch (JSONException e){

            }
        }

        return status;
    }
}
