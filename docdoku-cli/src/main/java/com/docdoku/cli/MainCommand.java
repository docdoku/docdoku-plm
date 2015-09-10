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

import com.docdoku.cli.commands.VersionOptionHandler;
import com.docdoku.cli.commands.common.AccountInfosCommand;
import com.docdoku.cli.commands.common.FileStatusCommand;
import com.docdoku.cli.commands.common.HelpCommand;
import com.docdoku.cli.commands.common.WorkspacesCommand;
import com.docdoku.cli.commands.documents.*;
import com.docdoku.cli.commands.parts.*;
import com.docdoku.cli.helpers.CommandLine;
import com.docdoku.core.common.Version;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Arrays;

public class MainCommand {


    private static final String PART = "part";
    private static final String DOCUMENT = "document";

    /**
     * Main function wrapper
     */
    public static void main(String[] args) {
        try {
            switch (args[0]) {
                case "status":
                case "stat":
                case "st":
                    if (PART.equals(args[1])) {
                        execCommand(new PartStatusCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentStatusCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new FileStatusCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                case "get":
                    if (PART.equals(args[1])) {
                        execCommand(new PartGetCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentGetCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                case "put":
                    if (PART.equals(args[1])) {
                        execCommand(new PartPutCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentPutCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                case "checkout":
                case "co":
                    if (PART.equals(args[1])) {
                        execCommand(new PartCheckOutCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentCheckOutCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                case "undocheckout":
                case "uco":
                    if (PART.equals(args[1])) {
                        execCommand(new PartUndoCheckOutCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentUndoCheckOutCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                case "checkin":
                case "ci":
                    if (PART.equals(args[1])) {
                        execCommand(new PartCheckInCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentCheckInCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;

                case "search":
                case "s":
                    if (PART.equals(args[1])) {
                        execCommand(new PartSearchCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentSearchCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;

                case "create":
                case "cr":
                    if (PART.equals(args[1])) {
                        execCommand(new PartCreationCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentCreationCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                case "list":
                case "l":
                    if (PART.equals(args[1])) {
                        execCommand(new PartListCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else if (DOCUMENT.equals(args[1])) {
                        execCommand(new DocumentListCommand(), Arrays.copyOfRange(args, 2, args.length));
                    } else {
                        execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;

                case "folders":
                case "f":
                    execCommand(new FolderListCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;

                case "baselinelist":
                case "bl":
                    execCommand(new BaselineListCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;

                case "conversion":
                case "cv":
                    execCommand(new ConversionCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;

                case "workspaces":
                case "wl":
                    execCommand(new WorkspacesCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;

                case "account":
                case "a":
                    execCommand(new AccountInfosCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;

                case "help":
                case "?":
                case "h":
                    execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    break;
                default:
                    execCommand(new HelpCommand(), args);
                    break;
            }
        } catch (Exception e) {
            execCommand(new HelpCommand(), args);
        }
    }

    private MainCommand() {
        super();
    }

    private static void execCommand(CommandLine cl, String[] args) {
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
