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

package com.docdoku.cli;

import com.docdoku.cli.commands.*;
import com.docdoku.core.common.Version;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Arrays;

public class MainCommand {
    private MainCommand(){
        super();
    }

  /*
  * Main function wrapper
  * */
    public static void main(String[] args) {
        try {
            switch (args[0]) {
                case "status":
                case "stat":
                case "st":
                    execCommand(new StatusCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "get":
                     execCommand(new GetCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "put":
                     execCommand(new PutCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "checkout":
                case "co":
                     execCommand(new CheckOutCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "undocheckout":
                case "uco":
                     execCommand(new UndoCheckOutCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "checkin":
                case "ci":
                     execCommand(new CheckInCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "create":
                case "cr":
                     execCommand(new PartCreationCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "partlist":
                case "pl":
                     execCommand(new PartListCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "search":
                case "s":
                     execCommand(new SearchPartsCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "workspaces":
                case "wl":
                     execCommand(new WorkspacesCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "baselinelist":
                case "bl":
                     execCommand(new BaselineListCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "conversion":
                case "cv":
                    execCommand(new ConversionCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "help":
                case "?":
                case "h":
                    if(args.length == 1){
                        execCommand(new HelpCommand(), args);
                    }else{
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                default:
                    execCommand(new HelpCommand(), args);
                    break;
            }
        } catch (Exception e) {
            execCommand(new HelpCommand(), args);
        }
    }

    private static void execCommand(AbstractCommandLine cl, String[] args) {
        CmdLineParser.registerHandler(Version.class, VersionOptionHandler.class);
        CmdLineParser parser = new CmdLineParser(cl);
        try {
            parser.parseArgument(args);
            cl.exec();
       } catch (Exception e) {
            cl.getOutput().printException(e);
        }
    }

}
