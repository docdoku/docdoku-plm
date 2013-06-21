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
import com.docdoku.cli.exceptions.StatusException;
import com.docdoku.cli.helpers.JSONPrinter;
import com.docdoku.cli.helpers.MetaDirectoryManager;
import com.docdoku.core.common.User;
import com.docdoku.core.common.Version;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartMaster;
import com.docdoku.core.product.PartMasterKey;
import com.docdoku.core.product.PartRevision;
import com.docdoku.core.services.IProductManagerWS;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatusCommand extends AbstractCommandLine{



    @Option(metaVar = "<revision>", name="-r", aliases = "--revision", usage="specify revision of the part to get a status ('A', 'B'...); if not specified the part identity (number and revision) corresponding to the cad file will be selected")
    private Version revision;

    @Option(metaVar = "<partnumber>", name = "-o", aliases = "--part", usage = "the part number of the part to get a status; if not specified choose the part corresponding to the cad file")
    private String partNumber;

    @Argument(metaVar = "[<cadfile>]", index=0, usage = "specify the cad file of the part to get a status")
    private File cadFile;

    @Override
    public void execImpl() throws Exception {
        try {
            if(partNumber==null || revision==null){
                loadMetadata();
            }

            IProductManagerWS productS = ScriptingTools.createProductService(getServerURL(), user, password);
            PartMaster pm = productS.getPartMaster(new PartMasterKey(workspace, partNumber));

            if (jsonParser) {
                JSONPrinter.printPartMasterStatus(pm);
            } else {
                printMasterStatus(pm);
            }
        } catch (StatusException e)  {
            JSONPrinter.printException(e);
        }
    }

    private void printMasterStatus(PartMaster pm){
        String partNumber = pm.getNumber()+"";
        String name = (pm.getName()==null || pm.getName().isEmpty())?"":" -" + pm.getName() + "-";
        System.out.println(partNumber  + name + " (" + pm.getWorkspaceId()+")");
        int revColSize = pm.getLastRevision().getVersion().length();
        String strRevision=revision.toString();
        for(PartRevision pr:pm.getPartRevisions()){
            if(pr.getVersion().equals(strRevision))
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

    private void loadMetadata() throws IOException, StatusException {
        if(cadFile==null){
            throw new IllegalArgumentException("<partnumber> or <revision> are not specified and no cad file is supplied");
        }

        MetaDirectoryManager meta = new MetaDirectoryManager(cadFile.getParentFile());
        String filePath = cadFile.getAbsolutePath();
        partNumber = meta.getPartNumber(filePath);
        String strRevision = meta.getRevision(filePath);
        if(partNumber==null || strRevision==null){
            if (jsonParser) {
                throw new StatusException("File is unversioned");
            }
            else {
                throw new IllegalArgumentException("<partnumber> or <revision> are not specified and cannot be inferred from file");
            }
        }

        revision = new Version(strRevision);
    }

    @Override
    public String getDescription() {
        return "Print the status of the selected part.";
    }
}
