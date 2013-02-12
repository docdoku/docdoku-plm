/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
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
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.util.*;


public class MainCommand {




        public static void main(String[] args) {

            try {
                switch(args[0]){
                    case "status": case "stat": case "st":
                        execCommand(new StatusCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    case "get":
                        execCommand(new GetCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    case "put":
                        execCommand(new PutCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    case "checkout": case "co":
                        execCommand(new CheckOutCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    case "undocheckout": case "uco":
                        execCommand(new UndoCheckOutCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    case "checkin": case "ci":
                        execCommand(new CheckInCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    case "help": case "?" : case "h":
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                        break;

                    default:
                        printUsage();
                }

            } catch (Exception e) {
                printUsage();
            }
        }



    private static void execCommand(CommandLine cl, String[] args){
        CmdLineParser.registerHandler(Version.class, VersionOptionHandler.class);
        CmdLineParser parser = new CmdLineParser(cl);
        try {
            parser.parseArgument(args);
            cl.exec();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

    }

    private static void printUsage() {
        System.err.println("usage: dplm <command> [<args>]");
        System.err.println("DocDokuPLM command-line client, version 1.0.");
        System.err.println("Type 'dplm help <command>' for help on a specific command.");
        System.err.println();
        System.err.println("Available commands:");
        System.err.println("   checkin (ci)");
        System.err.println("   checkout (co)");
        System.err.println("   get");
        System.err.println("   help (?, h)");
        System.err.println("   put");
        System.err.println("   status (stat, st)");
        System.err.println("   undocheckout (uco)");
        System.err.println();
        System.err.println("For additional information, see http://www.docdokuplm.com");
    }


}
