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

package com.docdoku.cli.commands.common;

import com.docdoku.cli.commands.AbstractCommandLine;
import com.docdoku.cli.commands.parts.*;
import com.docdoku.cli.helpers.CommandLine;
import com.docdoku.cli.helpers.LangHelper;
import org.kohsuke.args4j.Argument;

import java.io.IOException;

/**
 *
 * @author Florent Garin
 */
public class HelpCommand extends AbstractCommandLine {

    @Argument(metaVar = "<command>", index=0, usage = "the command to display the help information")
    private String command="";

    @Override
    public void execImpl() throws Exception {

        CommandLine cl;
        switch(command){
            case "status": case "stat": case "st":
                cl=new PartStatusCommand();
                break;

            case "get":
                cl=new PartGetCommand();
                break;

            case "put":
                cl=new PartPutCommand();
                break;

            case "checkout": case "co":
                cl=new PartCheckOutCommand();
                break;

            case "undocheckout": case "uco":
                cl=new PartUndoCheckOutCommand();
                break;

            case "checkin": case "ci":
                cl=new PartCheckInCommand();
                break;

            case "create": case "cr":
                cl=new PartCreationCommand();
                break;

            case "help": case "?" : case "h":
                cl=new HelpCommand();
                break;

            default:
                cl=null;
        }
        if(cl == null){
            output.printUsage();
            return;
        }
        output.printCommandUsage(cl);

    }

    @Override
    public String getDescription() throws IOException {
        return LangHelper.getLocalizedMessage("HelpCommandDescription", user);
    }
}
