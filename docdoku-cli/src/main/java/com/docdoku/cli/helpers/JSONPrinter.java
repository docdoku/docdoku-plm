package com.docdoku.cli.helpers;

import com.docdoku.core.common.User;
import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import java.util.List;

public class JSONPrinter {

    public static JSONObject getJSONPartMasterDescription(PartMaster pm, long lastModified) throws JSONException {
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
        JSONArray partIterationJSonArray;
        if (partIterations != null) {
            partIterationJSonArray = new JSONArray();
            for(PartIteration partIteration : partIterations) {
                partIterationJSonArray.put(partIteration.getIteration());
            }
            status.put("iterations", partIterationJSonArray);
        }

        return status;
    }

    public static void printPartMasterStatus(PartMaster pm, long lastModified) throws JSONException {
          System.out.println(getJSONPartMasterDescription(pm,lastModified).toString());

    }

    public static void printException(String message) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("dplmError", message);
        System.out.println(jsonObj);
    }

    public static JSONObject getJSONBaseline(Baseline baseline) throws JSONException {
        JSONObject baselineObject = new JSONObject();
        baselineObject.put("id", baseline.hashCode());
        baselineObject.put("name",baseline.getName());
        baselineObject.put("configurationItem",baseline.getConfigurationItem().getId());
        return baselineObject;
    }
}
