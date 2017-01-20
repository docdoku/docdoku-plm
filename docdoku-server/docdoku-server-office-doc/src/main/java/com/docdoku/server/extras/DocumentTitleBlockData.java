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

import com.docdoku.core.document.DocumentIteration;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author kelto on 05/01/16.
 *         <p>
 *         This class should be used to override the default Pdf generation for document.
 * @see com.docdoku.server.extras.TitleBlockGenerator
 */
class DocumentTitleBlockData extends TitleBlockData {

    DocumentTitleBlockData(DocumentIteration documentIteration, Locale pLocale) {
        locale = pLocale;
        bundle = ResourceBundle.getBundle(BASE_NAME, pLocale);
        dateFormat = new SimpleDateFormat(bundle.getString("date.format"));
        authorName = documentIteration.getAuthor().getName();
        version = documentIteration.getVersion();
        creationDate = dateFormat.format(documentIteration.getDocumentRevision().getCreationDate());
        iterationDate = dateFormat.format(documentIteration.getCreationDate());
        keywords = documentIteration.getDocumentRevision().getTags().toString();
        description = documentIteration.getDocumentRevision().getDescription();
        instanceAttributes = documentIteration.getInstanceAttributes();
        currentIteration = String.valueOf(documentIteration.getIteration());
        workflow = documentIteration.getDocumentRevision().getWorkflow();
        revisionNote = documentIteration.getRevisionNote();
        lifeCycleState = documentIteration.getDocumentRevision().getLifeCycleState();
        title = documentIteration.getId() + "-" + version;
        subject = documentIteration.getTitle();
    }

}
