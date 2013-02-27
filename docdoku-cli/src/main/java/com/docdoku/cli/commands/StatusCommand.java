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

package com.docdoku.cli.commands;


import com.docdoku.cli.ScriptingTools;
import com.docdoku.core.product.*;
import com.docdoku.core.services.IProductManagerWS;
import org.kohsuke.args4j.Argument;

import java.text.DateFormat;
import java.util.Locale;

public class StatusCommand extends AbstractCommandLine{



    @Argument(metaVar = "<partnumber>", required = true, index=0, usage = "the part number of the part to get a status")
    private String partNumber;


    @Override
    public void execImpl() throws Exception {
        IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
        PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, partNumber));

        printMasterStatus(pm);
        //System.out.println("Status part: " + pm.getNumber() + " " + partIPK.getPartRevision().getVersion() + "." + partIPK.getIteration() + " (" + workspace + ")");
    }

    private void printMasterStatus(PartMaster pm){
        String partNumber = pm.getNumber()+"";
        String name = (pm.getName()==null || pm.getName().isEmpty())?"":" -" + pm.getName() + "-";
        System.out.println(partNumber  + name + " (" + pm.getWorkspaceId()+")");
        int revColSize = pm.getLastRevision().getVersion().length();
        for(PartRevision pr:pm.getPartRevisions()){
            printRevisionStatus(revColSize,pr);
        }
    }

    private void printRevisionStatus(int revColSize, PartRevision pr){
        String revision = fillWithEmptySpace(pr.getVersion(),revColSize);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
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
    }

    private void printIterationStatus(PartIteration pi, int iteColSize, int dateColSize, int authorColSize){
        String iteration = fillWithEmptySpace(pi.getIteration()+"", iteColSize+1);
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.US);
        String date = fillWithEmptySpace(df.format(pi.getCreationDate()), dateColSize);
        String author = fillWithEmptySpace(pi.getAuthor()+"", authorColSize);
        String note = pi.getIterationNote()==null?"":pi.getIterationNote();
        System.out.println(iteration + " |" + date + " |" + author + " | " + note);
    }

    private String fillWithEmptySpace(String txt, int totalChar){
        StringBuilder b = new StringBuilder(txt);
        for(int i = 0; i < totalChar-txt.length();i++)
            b.insert(0,' ');

        return b.toString();
    }


    @Override
    public String getDescription() {
        return "Print the status of the selected part.";
    }
}
