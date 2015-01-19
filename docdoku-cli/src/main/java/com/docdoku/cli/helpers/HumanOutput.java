package com.docdoku.cli.helpers;

import com.docdoku.cli.commands.HelpCommand;
import com.docdoku.cli.interfaces.CommandLine;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class HumanOutput extends CliOutput{


    @Override
    public void printException(Exception e) {
        if(e instanceof CmdLineException){
            System.err.println(e.getMessage());
            printUsage();
        }else{
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void printCommandUsage(CommandLine cl) {
        CmdLineParser parser = new CmdLineParser(cl);
        System.out.println(cl.getDescription());
        System.out.println();
        parser.printUsage(System.out);
        System.out.println();
        if(cl instanceof HelpCommand){
            printAvailableCommands();
        }
    }

    @Override
    public void printUsage() {
        System.err.println("usage: dplm <command> [<args>]");
        System.err.println("DocDokuPLM command-line client, version 1.0.");
        System.err.println("Type 'dplm help <command>' for help on a specific command.");
        System.err.println();
        printAvailableCommands();
        System.err.println();
        System.err.println("For additional information, see http://www.docdokuplm.com");
    }

    private void printAvailableCommands(){
        System.err.println("Available commands:");
        System.err.println("   checkin (ci)");
        System.err.println("   checkout (co)");
        System.err.println("   create (cr)");
        System.err.println("   get");
        System.err.println("   help (?, h)");
        System.err.println("   put");
        System.err.println("   status (stat, st)");
        System.err.println("   undocheckout (uco)");
    }

    @Override
    public void printInfo(String s) {
        System.out.println(s);
    }

    @Override
    public void printWorkspaces(Workspace[] workspaces) {
        for(Workspace workspace:workspaces){
            System.out.println(workspace.getId());
        }
    }

    @Override
    public void printPartRevisionsCount(int partRevisionsCount) {
        System.out.println("Count : " + partRevisionsCount);
    }

    @Override
    public void printPartRevisions(List<PartRevision> partRevisions) {
        for(PartRevision pr: partRevisions) {
            printRevisionStatus(1, pr);
        }
    }

    @Override
    public void printBaselines(List<ProductBaseline> productBaselines) {
        if(productBaselines.isEmpty()){
            System.out.println("No baseline");
            return;
        }
        for(ProductBaseline productBaseline : productBaselines) {
            System.out.println("#" + productBaseline.getId() + " : " + productBaseline.getName());
        }
    }

    @Override
    public void printPartRevision(PartRevision pr, long lastModified) {
        printRevisionStatus(1,pr);
    }

    @Override
    public void printPartMaster(PartMaster pm, long lastModified) {
        int revColSize = pm.getLastRevision().getVersion().length();
        for(PartRevision pr:pm.getPartRevisions()){
            printRevisionStatus(revColSize,pr);
        }
    }

    @Override
    public FilterInputStream getMonitor(long maximum, InputStream in) {
        return new ConsoleProgressMonitorInputStream(maximum,in);
    }

    private String fillWithEmptySpace(String txt, int totalChar){
        StringBuilder b = new StringBuilder(txt);
        for(int i = 0; i < totalChar-txt.length();i++) {
            b.insert(0, ' ');
        }

        return b.toString();
    }

    private void printRevisionStatus(int revColSize, PartRevision pr){

        String revision = fillWithEmptySpace(pr.getVersion(),revColSize);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);

        System.out.println("# "+pr.getPartMasterNumber());

        String checkout = "";
        if(pr.isCheckedOut()){
            checkout = " checked out by " + pr.getCheckOutUser() + " on " + df.format(pr.getCheckOutDate());
        }
        System.out.println("Revision " + revision + checkout);
        int iteColSize = (pr.getLastIteration().getIteration() +"").length();
        int dateColSize=0;
        int authorColSize=0;

        for(PartIteration pi:pr.getPartIterations()){
            dateColSize = Math.max(dateColSize, df.format(pi.getCreationDate()).length());
            authorColSize = Math.max(authorColSize, pi.getAuthor().toString().length());
        }
        for(PartIteration pi:pr.getPartIterations()){
            printIterationStatus(pi, iteColSize, dateColSize +1, authorColSize+1);
        }
        System.out.println("");
    }

    private void printIterationStatus(PartIteration pi, int iteColSize, int dateColSize, int authorColSize){
        String iteration = fillWithEmptySpace(pi.getIteration()+"", iteColSize+1);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
        String date = fillWithEmptySpace(df.format(pi.getCreationDate()), dateColSize);
        String author = fillWithEmptySpace(pi.getAuthor()+"", authorColSize);
        String note = pi.getIterationNote()==null?"":pi.getIterationNote();
        System.out.println(iteration + " |" + date + " |" + author + " | " + note);
    }


}
