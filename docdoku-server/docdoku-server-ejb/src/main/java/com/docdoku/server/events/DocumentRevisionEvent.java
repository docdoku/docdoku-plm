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
package com.docdoku.server.events;

import com.docdoku.core.document.DocumentRevision;

/**
 * @author Florent Garin
 */
public class DocumentRevisionEvent {

    private DocumentRevision observedDocument;

    public DocumentRevisionEvent(DocumentRevision observedDocument) {
        this.observedDocument = observedDocument;
    }

    public DocumentRevision getObservedDocument() {
        return observedDocument;
    }

    public void setObservedDocument(DocumentRevision observedDocument) {
        this.observedDocument = observedDocument;
    }
}
