package com.docdoku.cli.helpers;

import com.docdoku.cli.exceptions.StatusException;
import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.common.User;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import java.util.List;

public class JSONPrinter {

    public static JSONObject getJSONPartMasterDescription(PartMaster pm) throws JSONException {
        JSONObject status = new JSONObject();

        status.put("isCheckedOut", pm.getLastRevision().isCheckedOut());
        status.put("partNumber", pm.getNumber());
        User user = pm.getLastRevision().getCheckOutUser();
        String login = user != null ? user.getLogin() : "";
        status.put("checkoutUser", login);
        Date checkoutDate = pm.getLastRevision().getCheckOutDate();
        Long timeStamp = checkoutDate != null ? checkoutDate.getTime() : null;
        status.put("checkoutDate", timeStamp);
        status.put("workspace", pm.getWorkspace().getId());
        status.put("version", pm.getLastRevision().getVersion());
        status.put("description", pm.getLastRevision().getDescription());
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

    public static void printPartMasterStatus(PartMaster pm) throws JSONException {
          System.out.println(getJSONPartMasterDescription(pm).toString());

    }

    public static void printException(Exception e) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("dplmError", e.getMessage());
        System.out.println(jsonObj);
    }
}
