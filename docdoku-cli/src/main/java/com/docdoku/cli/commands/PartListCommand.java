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

    @Option(name="-c", aliases = "--count", usage="return part master count in workspace")
    private boolean count;

    @Option(name="-s", aliases = "--start", usage="start offset")
    private int start;

    @Option(name="-m", aliases = "--max-results", usage="max results")
    private int max;

    @Option(name="-j", aliases = "--jsonparser", usage="return the list of the parts in JSON format")
    private boolean jsonParser;


    @Override
    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);

        if(count){
            int partMastersCount = productS.getPartMastersCount(workspace);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("count",partMastersCount);
            System.out.println(jsonObject.toString());
        }else{
            List<PartMaster> partMasters = productS.getPartMasters(workspace, start, max);
            JSONArray jsonArray = new JSONArray();

            for(PartMaster partMaster : partMasters) {
                for(PartRevision partRevision : partMaster.getPartRevisions()) {
                    jsonArray.put(JSONPrinter.getJSONPartMasterDescription(partMaster,0L));
                }
            }

            System.out.println(jsonArray.toString());
        }

    }

    @Override
    public String getDescription() {
        return null;
    }
}
