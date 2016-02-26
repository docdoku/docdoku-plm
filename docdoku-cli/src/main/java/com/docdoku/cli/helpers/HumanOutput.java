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
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

public class HumanOutput extends CliOutput{

    private Locale locale;
    private PrintStream ERROR_STREAM = System.err;
    private PrintStream OUTPUT_STREAM = System.out;
    public HumanOutput(Locale pLocale) {
        locale = pLocale;
    }

    @Override
    public void printException(Exception e) {
        ERROR_STREAM.println(e.getMessage());
        if(e instanceof CmdLineException){
            printUsage();
        }
    }

    @Override
    public void printCommandUsage(CommandLine cl) throws IOException {
        CmdLineParser parser = new CmdLineParser(cl);
        OUTPUT_STREAM.println(cl.getDescription());
        OUTPUT_STREAM.println();
        parser.printUsage(OUTPUT_STREAM);
        OUTPUT_STREAM.println();
    }

    @Override
    public void printUsage() {
        ERROR_STREAM.println(LangHelper.getLocalizedMessage("Usage",locale));
        ERROR_STREAM.println();
        printAvailableCommands();
        ERROR_STREAM.println();
        ERROR_STREAM.println(LangHelper.getLocalizedMessage("AdditionalInfos",locale));
    }

    private void printAvailableCommands(){
        ERROR_STREAM.println(LangHelper.getLocalizedMessage("AvailableCommands",locale) +":");
        ERROR_STREAM.println("   checkin (ci)");
        ERROR_STREAM.println("   checkout (co)");
        ERROR_STREAM.println("   create (cr)");
        ERROR_STREAM.println("   get");
        ERROR_STREAM.println("   help (?, h)");
        ERROR_STREAM.println("   put");
        ERROR_STREAM.println("   status (stat, st)");
        ERROR_STREAM.println("   undocheckout (uco)");
        ERROR_STREAM.println();
        ERROR_STREAM.println(LangHelper.getLocalizedMessage("InstructionCommands",locale));
    }

    @Override
    public void printInfo(String s) {
        OUTPUT_STREAM.println(s);
    }

    @Override
    public void printWorkspaces(Workspace[] workspaces) {
        for(Workspace workspace:workspaces){
            OUTPUT_STREAM.println(workspace.getId());
        }
    }

    @Override
    public void printPartRevisionsCount(int partRevisionsCount) {
        OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("Count",locale) + " : " + partRevisionsCount);
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
            OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("NoBaseline",locale));
            return;
        }
        for(ProductBaseline productBaseline : productBaselines) {
            OUTPUT_STREAM.println("#" + productBaseline.getId() + " : " + productBaseline.getName());
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
            OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("ConversionSucceed",locale));
        }else if(conversion.isPending()){
            OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("ConversionInProgress",locale));
        } else{
            OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("ConversionFailed",locale));
        }
        OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("ConversionStarted",locale) + " : " + conversion.getStartDate());
        OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("ConversionEnded",locale) + " : " + conversion.getEndDate());
    }

    @Override
    public void printAccount(Account account) throws JSONException {
        OUTPUT_STREAM.println(account.getLogin());
        OUTPUT_STREAM.println(account.getEmail());
        OUTPUT_STREAM.println(account.getLanguage());
        OUTPUT_STREAM.println(account.getTimeZone());
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
            OUTPUT_STREAM.println(folder);
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

        OUTPUT_STREAM.println("# "+pr.getPartMasterNumber());

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
        OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("Revision",locale) + " " + revision + checkout);
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
        OUTPUT_STREAM.println("");
    }

    private void printIterationStatus(PartIteration pi, int iteColSize, int dateColSize, int authorColSize){
        String iteration = fillWithEmptySpace(pi.getIteration()+"", iteColSize+1);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
        String date = fillWithEmptySpace(df.format(pi.getCreationDate()), dateColSize);
        String author = fillWithEmptySpace(pi.getAuthor()+"", authorColSize);
        String note = pi.getIterationNote()==null?"":pi.getIterationNote();
        OUTPUT_STREAM.println(iteration + " |" + date + " |" + author + " | " + note);
    }


    private void printRevisionStatus(int revColSize, DocumentRevision dr){

        String revision = fillWithEmptySpace(dr.getVersion(),revColSize);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);

        OUTPUT_STREAM.println("# " + dr.getDocumentMasterId());

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
        OUTPUT_STREAM.println(LangHelper.getLocalizedMessage("Revision",locale) + " " + revision + checkout);
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
        OUTPUT_STREAM.println("");
    }

    private void printIterationStatus(DocumentIteration di, int iteColSize, int dateColSize, int authorColSize){
        String iteration = fillWithEmptySpace(di.getIteration()+"", iteColSize+1);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
        String date = fillWithEmptySpace(df.format(di.getCreationDate()), dateColSize);
        String author = fillWithEmptySpace(di.getAuthor()+"", authorColSize);
        String note = di.getRevisionNote()==null?"" : di.getRevisionNote();
        OUTPUT_STREAM.println(iteration + " |" + date + " |" + author + " | " + note);
    }
}
