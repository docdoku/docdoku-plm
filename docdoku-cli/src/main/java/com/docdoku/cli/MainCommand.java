package com.docdoku.cli;

import com.docdoku.cli.commands.*;
import com.docdoku.cli.helpers.JSONOutput;
import com.docdoku.core.common.Version;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MainCommand {
    private static final Logger LOGGER = Logger.getLogger(MainCommand.class.getName());

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
                case "help":
                case "?":
                case "h":
                    if (args.length > 1) {
                         execCommand(new HelpCommand(), Arrays.copyOfRange(args, 1, args.length));
                    }
                    break;
                default:
                    printUsage();
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINEST,null,e);
            printUsage();
        }
    }

    private static void execCommand(AbstractCommandLine cl, String[] args) {
        CmdLineParser.registerHandler(Version.class, VersionOptionHandler.class);
        CmdLineParser parser = new CmdLineParser(cl);
        try {
            parser.parseArgument(args);
            cl.exec();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            JSONOutput.printException(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            JSONOutput.printException(e);
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
