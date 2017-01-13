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

import com.docdoku.core.product.PartIteration;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author kelto on 05/01/16.
 *         <p>
 *         This class should be used to override the default Pdf generation for parts.
 * @see com.docdoku.server.extras.TitleBlockGenerator
 */
public class PartTitleBlockData extends TitleBlockData {

    PartTitleBlockData(PartIteration partIteration, Locale locale) {
        pLocale = locale;
        bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        dateFormat = new SimpleDateFormat(bundle.getString("date.format"));
        authorName = partIteration.getAuthor().getName();
        version = partIteration.getVersion();
        creationDate = dateFormat.format(partIteration.getPartRevision().getCreationDate());
        iterationDate = dateFormat.format(partIteration.getCreationDate());
        keywords = partIteration.getPartRevision().getTags().toString();
        description = partIteration.getPartRevision().getDescription();
        instanceAttributes = partIteration.getInstanceAttributes();
        currentIteration = String.valueOf(partIteration.getIteration());
        workflow = partIteration.getPartRevision().getWorkflow();
        revisionNote = partIteration.getIterationNote();
        lifeCycleState = partIteration.getPartRevision().getLifeCycleState();
        title = partIteration.getNumber() + "-" + version;
        subject = partIteration.getName();
    }
}
