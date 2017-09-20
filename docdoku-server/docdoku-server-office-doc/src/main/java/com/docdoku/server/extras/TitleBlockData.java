/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2017 DocDoku SARL
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

package com.docdoku.server.extras;

import com.docdoku.core.meta.InstanceAttribute;
import com.docdoku.core.workflow.Workflow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 * @author Morgan Guimard
 *         <p>
 *         This class define the default pdf generation for both part and document.
 *         This behaviour can be overridden:
 * @see PartTitleBlockData
 * @see DocumentTitleBlockData
 */
public abstract class TitleBlockData {

    protected static final String PROPERTIES_BASE_NAME = "/com/docdoku/server/extras/localization/TitleBlockData";

    protected Properties properties;
    protected SimpleDateFormat dateFormat;
    protected String title;
    protected String subject;
    protected String authorName;
    protected String version;
    protected String creationDate;
    protected String iterationDate;
    protected String keywords;
    protected String description;
    protected List<InstanceAttribute> instanceAttributes;
    protected String currentIteration;
    protected Workflow workflow;
    protected Locale locale;
    protected String revisionNote;
    protected String lifeCycleState;

    public String getBundleString(String key) {
        return properties.getProperty(key);
    }

    public String format(Date date) {
        return dateFormat.format(date);
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getVersion() {
        return version;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getIterationDate() {
        return iterationDate;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getDescription() {
        return description;
    }

    public List<InstanceAttribute> getInstanceAttributes() {
        return instanceAttributes;
    }

    public String getCurrentIteration() {
        return currentIteration;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getRevisionNote() {
        return revisionNote;
    }
}
