package com.docdoku.cli.commands;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.helpers.JSONPrinter;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IProductManagerWS;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.kohsuke.args4j.Option;

import java.util.List;

public class PartListCommand extends AbstractCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(name="-j", aliases = "--jsonparser", usage="return the list of the parts in JSON format")
    private boolean jsonParser;


    @Override
    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        List<PartMaster> partMasters = productS.getPartMasters(workspace, 0, 10000);
        JSONArray jsonArray = new JSONArray();

        for(PartMaster partMaster : partMasters) {
            for(PartRevision partRevision : partMaster.getPartRevisions()) {
                JSONObject jsonObject = new JSONObject();
                jsonArray.put(JSONPrinter.getJSONPartMasterDescription(partMaster));
            }
        }

        System.out.println(jsonArray.toString());
    }

    @Override
    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
