package com.docdoku.cli;

import com.docdoku.cli.commands.*;
import com.docdoku.cli.helpers.JSONOutput;
import com.docdoku.core.common.Version;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Arrays;


public class MainCommandImpl {
  /*
  * Main function wrapper
  * */
    public static Object main(String[] args) {
        try {
            switch (args[0]) {
                case "status":
                case "stat":
                case "st":
                    return execCommand(new StatusCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "get":
                    return execCommand(new GetCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "put":
                    return execCommand(new PutCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "checkout":
                case "co":
                    return execCommand(new CheckOutCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "undocheckout":
                case "uco":
                    return execCommand(new UndoCheckOutCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "checkin":
                case "ci":
                    return execCommand(new CheckInCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "create":
                case "cr":
                    return execCommand(new PartCreationCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "partlist":
                case "pl":
                    return execCommand(new PartListCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "search":
                case "s":
                    return execCommand(new SearchPartsCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "workspaces":
                case "wl":
                    return execCommand(new WorkspacesCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "baselinelist":
                case "bl":
                    return execCommand(new BaselineListCommand(), Arrays.copyOfRange(args, 1, args.length));
                case "help":
                case "?":
                case "h":
                    if (args.length > 1) {
                        return execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                default:
                    printUsage();
                    return null;
            }
        } catch (Exception e) {
            printUsage();
            return null;
        }
    }

    private static Object execCommand(AbstractCommandLine cl, String[] args) {
        CmdLineParser.registerHandler(Version.class, VersionOptionHandler.class);
        CmdLineParser parser = new CmdLineParser(cl);
        try {
            parser.parseArgument(args);
            return cl.exec();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return JSONOutput.printException(e);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return JSONOutput.printException(e);
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
        System.err.println("   create (cr)");
        System.err.println("   get");
        System.err.println("   help (?, h)");
        System.err.println("   put");
        System.err.println("   status (stat, st)");
        System.err.println("   undocheckout (uco)");
        System.err.println();
        System.err.println("For additional information, see http://www.docdokuplm.com");
    }

}
