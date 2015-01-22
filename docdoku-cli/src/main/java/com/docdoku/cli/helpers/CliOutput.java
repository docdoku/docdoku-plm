package com.docdoku.cli.helpers;

import com.docdoku.cli.interfaces.CommandLine;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import org.codehaus.jettison.json.JSONException;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.List;

import static com.docdoku.cli.helpers.CliOutput.formats.HUMAN;

/**
 * @author Morgan GUIMARD
 * @version 1.0, 14/11/14
 * @since   V2.0
 */
public abstract class CliOutput {

    public static enum formats{
        HUMAN,
        JSON
    }
    public static CliOutput getOutput(formats pFormat) {
        formats format = pFormat;
        if(format == null){
            format = HUMAN;
        }
        switch(format){
            case HUMAN: return new HumanOutput();
            case JSON: return new JSONOutput();
            default: return new HumanOutput();
        }
    }

    public abstract void printException(Exception e);
    public abstract void printCommandUsage(CommandLine cl);
    public abstract void printUsage();
    public abstract void printInfo(String s);

    public abstract void printWorkspaces(Workspace[] workspaces);
    public abstract void printPartRevisionsCount(int partRevisionsCount);
    public abstract void printPartRevisions(List<PartRevision> partRevisions);
    public abstract void printBaselines(List<ProductBaseline> productBaselines);
    public abstract void printPartRevision(PartRevision pr, long lastModified);
    public abstract void printPartMaster(PartMaster pm, long lastModified);
    public abstract void printConversion(Conversion conversion) throws JSONException;

    public abstract FilterInputStream getMonitor(long maximum, InputStream in);

}
