package com.docdoku.cli.commands;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.helpers.JSONPrinter;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.product.PartSearchQuery;
import com.docdoku.core.services.IProductManagerWS;
import org.codehaus.jettison.json.JSONArray;
import org.kohsuke.args4j.Option;

import java.util.List;

public class SearchPartsCommand extends AbstractCommandLine {

    @Option(name = "-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage = "workspace on which operations occur")
    protected String workspace;

    @Option(name = "-s", aliases = "--search", required = true, metaVar = "<search>", usage = "search string")
    protected String searchValue;

    @Option(name = "-j", aliases = "--jsonparser", usage = "return the list of the parts in JSON format")
    private boolean jsonParser;


    @Override
    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);

        List<PartRevision> partRevisions = productS.searchPartRevisions(
                new PartSearchQuery(workspace, searchValue, null, null, null, null, null, null, null, null)
        );

        JSONArray jsonArray = new JSONArray();

        for (PartRevision partRevision : partRevisions) {
            jsonArray.put(JSONPrinter.getJSONPartRevisionDescription(partRevision, 0L));
        }

        System.out.println(jsonArray.toString());
    }

    @Override
    public String getDescription() {
        return null;
    }
}
