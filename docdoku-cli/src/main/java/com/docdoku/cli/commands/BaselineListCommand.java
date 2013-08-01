package com.docdoku.cli.commands;

import com.docdoku.cli.ScriptingTools;
import com.docdoku.cli.helpers.JSONPrinter;
import com.docdoku.core.configuration.Baseline;
import com.docdoku.core.product.PartRevisionKey;
import com.docdoku.core.services.IProductManagerWS;
import org.codehaus.jettison.json.JSONArray;
import org.kohsuke.args4j.Option;

import java.util.List;

public class BaselineListCommand extends AbstractCommandLine {

    @Option(name="-w", aliases = "--workspace", required = true, metaVar = "<workspace>", usage="workspace on which operations occur")
    protected String workspace;

    @Option(metaVar = "<partnumber>", required = true, name = "-o", aliases = "--part", usage = "the part number of the part to fetch; if not specified choose the part corresponding to the cad file")
    private String number;

    @Option(metaVar = "<revision>", required = true, name="-r", aliases = "--revision", usage="specify revision of the part to retrieve ('A', 'B'...); default is the latest")
    private String revision;

    @Option(name="-j", aliases = "--jsonparser", usage="return the list of the baselines in JSON format")
    private boolean jsonParser;

    private IProductManagerWS productS;

    @Override
    public void execImpl() throws Exception {
        productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartRevisionKey pK = new PartRevisionKey(workspace,number,revision);
        List<Baseline> baselines = productS.findBaselinesWherePartRevisionHasIterations(pK);
        JSONArray jsonArray = new JSONArray();

        for(Baseline baseline : baselines) {
            jsonArray.put(JSONPrinter.getJSONBaseline(baseline));
        }

        System.out.println(jsonArray.toString());
    }

    @Override
    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
