/*
 * DocDoku, Professional Open Source
 * Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2012 DocDoku SARL
 *
 * This file is part of DocDoku.
 *
 * DocDoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDoku is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
 * GNU General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with DocDoku.  If not, see <http://www.gnu.org/licenses/>.  
 */

package com.docdoku.server.http;

import com.docdoku.core.services.IDocumentManagerLocal;
import com.docdoku.core.workflow.Task;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Version;

public class CalendarServlet extends HttpServlet {

    private final static String PSEUDO_FILENAME = "DocDokuCalendar.ics";
    private final static long ONE_YEAR_IN_MILLISECONDS = 1000 * 60 * 60 * 24 * 365;
    @EJB
    private IDocumentManagerLocal documentService;

    @Override
    protected void doGet(HttpServletRequest pRequest,
            HttpServletResponse pResponse)
            throws ServletException, IOException {

        try {
            String[] pathInfos = Pattern.compile("/").split(pRequest.getRequestURI());
            int offset;
            if (pRequest.getContextPath().equals("")) {
                offset = 2;
            } else {
                offset = 3;
            }

            String workspaceId = URLDecoder.decode(pathInfos[offset], "UTF-8");

            Task[] tasks = documentService.getTasks(workspaceId);

            Calendar cal = new Calendar();
            //cal.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
            cal.getProperties().add(Version.VERSION_2_0);
            cal.getProperties().add(CalScale.GREGORIAN);
            java.util.Date now = new java.util.Date();
            for (Task task : tasks) {
                if (task.isInProgress()) {
                    String title = task.getTitle() == null ? "" : task.getTitle();
                    String instructions = task.getInstructions() == null ? "" : task.getInstructions();
                    String summary = " [" + title + "] " + instructions;
                    VEvent event = new VEvent(
                            new net.fortuna.ical4j.model.Date(task.getStartDate()),
                            new Dur(task.getDuration(), 0, 0, 0),
                            summary);
                    cal.getComponents().add(event);
                }
            }

            pResponse.setCharacterEncoding("UTF-8");
            pResponse.setContentType("text/calendar");
            pResponse.setHeader("Content-disposition", "attachment; filename=\"" + PSEUDO_FILENAME + "\"");

            pResponse.getWriter().print(cal);
            pResponse.flushBuffer();
        } catch (Exception pEx) {
            throw new ServletException("Error while fetching your tasks.", pEx);
        }

    }
}
