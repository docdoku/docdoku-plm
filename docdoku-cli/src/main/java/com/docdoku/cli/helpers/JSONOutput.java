/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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

import com.docdoku.core.common.User;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import java.util.List;

public class JSONOutput {

    public static String printPartMasters(List<PartMaster> partMasters) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(PartMaster partMaster : partMasters) {
            for(PartRevision partRevision : partMaster.getPartRevisions()) {
                jsonArray.put(getPartMaster(partMaster, 0L));
            }
        }
        return jsonArray.toString();
    }

    public static String printPartMaster(PartMaster pm, long lastModified) throws JSONException {
        return getPartMaster(pm,lastModified).toString();
    }

    public static JSONObject getPartMaster(PartMaster pm, long lastModified) throws JSONException {
        JSONObject status = new JSONObject();

        User user = pm.getLastRevision().getCheckOutUser();
        String login = user != null ? user.getLogin() : "";
        Date checkoutDate = pm.getLastRevision().getCheckOutDate();
        Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;

        status.put("isCheckedOut", pm.getLastRevision().isCheckedOut());
        status.put("partNumber", pm.getNumber());
        status.put("checkoutUser", login);
        status.put("checkoutDate", timeStamp);
        status.put("workspace", pm.getWorkspace().getId());
        status.put("version", pm.getLastRevision().getVersion());
        status.put("description", pm.getLastRevision().getDescription());
        status.put("lastModified", lastModified);

        if(pm.getLastRevision() != null && pm.getLastRevision().getLastIteration() != null && pm.getLastRevision().getLastIteration().getNativeCADFile() != null) {
            String nativeCADFileName  = pm.getLastRevision().getLastIteration().getNativeCADFile().getName();
            status.put("cadFileName", nativeCADFileName);
        }

        List<PartIteration> partIterations = pm.getLastRevision().getPartIterations();

        if (partIterations != null) {
            JSONArray partIterationJSonArray = new JSONArray();
            for(PartIteration partIteration : partIterations) {
                partIterationJSonArray.put(partIteration.getIteration());
            }
            status.put("iterations", partIterationJSonArray);
        }

        return status;
    }

    public static String printPartRevisions(List<PartRevision> partRevisions) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (PartRevision partRevision : partRevisions) {
            jsonArray.put(getPartRevision(partRevision, 0L));
        }
        return jsonArray.toString();
    }

    public static String printPartRevision(PartRevision partRevision, long lastModified) throws JSONException {
        return getPartRevision(partRevision,lastModified).toString();
    }

    public static JSONObject getPartRevision(PartRevision pr, long lastModified) throws JSONException {

        JSONObject status = new JSONObject();

        if(pr != null){
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
        }

        return status;
    }

    public static String printException(Exception e){
        try{
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("error", e.getMessage());
            return jsonObj.toString();
        }catch (JSONException ex){
            return null;
        }
    }

    public static String printBaselines(List<Baseline> baselines) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(Baseline baseline : baselines) {
            jsonArray.put(printBaseline(baseline));
        }
        return jsonArray.toString();
    }

    public static String printBaseline(Baseline baseline) throws JSONException {
        JSONObject baselineObject = new JSONObject();
        baselineObject.put("id", baseline.hashCode());
        baselineObject.put("name",baseline.getName());
        baselineObject.put("configurationItem",baseline.getConfigurationItem().getId());
        return baselineObject.toString();
    }

    public static String printWorkspaces(Workspace[] workspaces) throws JSONException {
        JSONArray wks = new JSONArray();
        for(int i = 0 ; i < workspaces.length; i++){
            wks.put(i,workspaces[i].getId());
        }
        return wks.toString();
    }

    public static String printPartRevisionsCount(int partMastersCount) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count",partMastersCount);
        return jsonObject.toString();
    }


}
