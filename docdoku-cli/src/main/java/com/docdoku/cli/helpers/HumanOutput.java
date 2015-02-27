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

package com.docdoku.cli.helpers;

import com.docdoku.cli.commands.common.HelpCommand;
import com.docdoku.cli.interfaces.CommandLine;
import com.docdoku.core.common.Account;
import com.docdoku.core.common.Workspace;
import com.docdoku.core.configuration.ProductBaseline;
import com.docdoku.core.document.DocumentIteration;
import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.product.Conversion;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartRevision;
import org.codehaus.jettison.json.JSONException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class HumanOutput extends CliOutput{

    private Locale locale;

    public HumanOutput(Locale pLocale) {
        locale = pLocale;
    }

    @Override
    public void printException(Exception e) {
        System.err.println(e.getMessage());
        if(e instanceof CmdLineException){
            printUsage();
        }
    }

    @Override
    public void printCommandUsage(CommandLine cl) throws IOException {
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
        System.err.println(LangHelper.getLocalizedMessage("Usage",locale));
        System.err.println();
        printAvailableCommands();
        System.err.println();
        System.err.println(LangHelper.getLocalizedMessage("AdditionalInfos",locale));
    }

    private void printAvailableCommands(){
        System.err.println(LangHelper.getLocalizedMessage("AvailableCommands",locale) +" :");
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
        System.out.println(LangHelper.getLocalizedMessage("Count",locale) + " : " + partRevisionsCount);
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
            System.out.println(LangHelper.getLocalizedMessage("NoBaseline",locale));
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
    public void printConversion(Conversion conversion) {
        if(conversion.isSucceed()){
            System.out.println(LangHelper.getLocalizedMessage("ConversionSucceed",locale));
        }else if(conversion.isPending()){
            System.out.println(LangHelper.getLocalizedMessage("ConversionInProgress",locale));
        } else{
            System.out.println(LangHelper.getLocalizedMessage("ConversionFailed",locale));
        }
        System.out.println(LangHelper.getLocalizedMessage("ConversionStarted",locale) + " : " + conversion.getStartDate());
        System.out.println(LangHelper.getLocalizedMessage("ConversionEnded",locale) + " : " + conversion.getEndDate());
    }

    @Override
    public void printAccount(Account account) throws JSONException {
        System.out.println(account.getLogin());
        System.out.println(account.getEmail());
        System.out.println(account.getLanguage());
        System.out.println(account.getTimeZone());
    }

    @Override
    public void printDocumentRevision(DocumentRevision dr, long lastModified) {
        printRevisionStatus(1,dr);
    }

    @Override
    public void printDocumentRevisions(DocumentRevision[] documentRevisions) {
        for(DocumentRevision dr: documentRevisions) {
            printDocumentRevision(dr, 1);
        }
    }

    @Override
    public void printFolders(String[] folders) {
        for (String folder : folders) {
            System.out.println(folder);
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
            checkout = " "
                    + LangHelper.getLocalizedMessage("CheckedOutBy",locale)
                    + " "
                    + pr.getCheckOutUser()
                    + " "
                    + LangHelper.getLocalizedMessage("On",locale)
                    + " "
                    + df.format(pr.getCheckOutDate());
        }
        System.out.println(LangHelper.getLocalizedMessage("Revision",locale) + " " + revision + checkout);
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


    private void printRevisionStatus(int revColSize, DocumentRevision dr){

        String revision = fillWithEmptySpace(dr.getVersion(),revColSize);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);

        System.out.println("# " + dr.getDocumentMasterId());

        String checkout = "";
        if(dr.isCheckedOut()){
            checkout = " "
                    + LangHelper.getLocalizedMessage("CheckedOutBy",locale)
                    + " "
                    + dr.getCheckOutUser()
                    + " "
                    + LangHelper.getLocalizedMessage("On",locale)
                    + " "
                    + df.format(dr.getCheckOutDate());
        }
        System.out.println(LangHelper.getLocalizedMessage("Revision",locale) + " " + revision + checkout);
        int iteColSize = (dr.getLastIteration().getIteration() +"").length();
        int dateColSize=0;
        int authorColSize=0;

        for(DocumentIteration di:dr.getDocumentIterations()){
            dateColSize = Math.max(dateColSize, df.format(di.getCreationDate()).length());
            authorColSize = Math.max(authorColSize, di.getAuthor().toString().length());
        }
        for(DocumentIteration di:dr.getDocumentIterations()){
            printIterationStatus(di, iteColSize, dateColSize +1, authorColSize+1);
        }
        System.out.println("");
    }

    private void printIterationStatus(DocumentIteration di, int iteColSize, int dateColSize, int authorColSize){
        String iteration = fillWithEmptySpace(di.getIteration()+"", iteColSize+1);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
        String date = fillWithEmptySpace(df.format(di.getCreationDate()), dateColSize);
        String author = fillWithEmptySpace(di.getAuthor()+"", authorColSize);
        String note = di.getRevisionNote()==null?"" : di.getRevisionNote();
        System.out.println(iteration + " |" + date + " |" + author + " | " + note);
    }
}
