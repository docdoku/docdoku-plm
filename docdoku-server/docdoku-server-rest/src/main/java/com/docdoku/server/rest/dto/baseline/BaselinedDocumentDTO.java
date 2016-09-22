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

package com.docdoku.server.rest.dto.baseline;

import com.docdoku.core.document.DocumentIteration;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
public class BaselinedDocumentDTO implements Serializable {

    private String documentMasterId;
    private String title;
    private String version;
    private int iteration;
    private List<BaselinedDocumentOptionDTO> availableIterations;

    public BaselinedDocumentDTO() {
    }

    public BaselinedDocumentDTO(DocumentIteration documentIteration) {
        this.documentMasterId = documentIteration.getDocumentMasterId();
        this.version = documentIteration.getVersion();
        this.title = documentIteration.getDocumentRevision().getTitle();
        this.iteration = documentIteration.getIteration();
    }

    public BaselinedDocumentDTO(List<DocumentIteration> availableDocuments) {

        DocumentIteration max = Collections.max(availableDocuments);

        this.documentMasterId = max.getDocumentMasterId();
        this.version = max.getVersion();
        this.title = max.getDocumentRevision().getTitle();
        this.iteration = max.getIteration();

        this.availableIterations = new ArrayList<>();
        for (DocumentIteration documentIteration : availableDocuments) {
            this.availableIterations.add(new BaselinedDocumentOptionDTO(documentIteration.getVersion(), documentIteration.getIteration(), documentIteration.getDocumentRevision().isReleased(), documentIteration.getDocumentRevision().isObsolete()));
        }
    }

    public BaselinedDocumentDTO(String number, String version, int iteration) {
        this.documentMasterId = number;
        this.version = version;
        this.iteration = iteration;
    }

    public String getDocumentMasterId() {
        return documentMasterId;
    }

    public void setDocumentMasterId(String documentMasterId) {
        this.documentMasterId = documentMasterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public List<BaselinedDocumentOptionDTO> getAvailableIterations() {
        return availableIterations;
    }

    public void setAvailableIterations(List<BaselinedDocumentOptionDTO> availableIterations) {
        this.availableIterations = availableIterations;
    }
}