package com.docdoku.cli.helpers;

import static com.docdoku.cli.helpers.CliOutput.formats.*;

/**
 * Created by morgan on 14/11/14.
 */
public abstract class CliOutput {

    public static enum formats{
        HUMAN,
        JSON
    }

    public static CliOutput GetOutput(formats format) {
        if(format == null){
            format = HUMAN;
        }
        switch(format){
            case HUMAN: return new HumanOutput();
            case JSON: return new JSONOutput();
            default: return new HumanOutput();
        }
    }

    public abstract void printWorkspaces();
}
